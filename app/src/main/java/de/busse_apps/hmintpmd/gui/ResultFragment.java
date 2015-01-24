package de.busse_apps.hmintpmd.gui;

/*
 * Copyright 2015 Bernd Busse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.busse_apps.hmintpmd.R;
import de.busse_apps.hmintpmd.common.WSIKMCalculator;
import de.busse_apps.hmintpmd.widget.CircleMeterDrawingThread;
import de.busse_apps.hmintpmd.widget.CircleMeterView;

public class ResultFragment extends Fragment implements CircleMeterView.CircleMeterCallback {
    
    public static final String ARGUMENT_VALUE = "de.busse_apps.hmintpmd.gui.ResultFragment.calcValue";
    
    private static final String SIS_FINISHED = "de.busse_apps.hmintpmd.gui.ResultFragment.finished";
    
    private TextView mResultHeader;
    private TextView mResultValue;

    private CircleMeterDrawingThread mDrawingThread;
    private CircleMeterView mResultGraph;
    private ShareActionProvider mShareActionProvider;
    
    private boolean mShouldUpdate;
    
    private boolean mFinished = false;
    
    private double mCalcValue;
    private String[] mMessages;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        Bundle args = getArguments();
        if (args != null) {
            mCalcValue = args.getDouble(ARGUMENT_VALUE);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mResultHeader = (TextView) getView().findViewById(R.id.result_header);
        mResultHeader.setText(R.string.result_wait);
        mResultValue = (TextView) getView().findViewById(R.id.result_value);
        
        mResultGraph = (CircleMeterView) getView().findViewById(R.id.result_graph);
        mResultGraph.setMaxValue(mCalcValue);
        mResultGraph.setMaxDegree(WSIKMCalculator.getDegreeForValue(mCalcValue));
        mResultGraph.setCallback(this);
        
        mDrawingThread = mResultGraph.getThread();
        
        mMessages = getResources().getStringArray(R.array.result_levels);
        
        if (savedInstanceState != null) {
            mDrawingThread.restoreState(savedInstanceState);
            mFinished = savedInstanceState.getBoolean(SIS_FINISHED);
        }
        if (mFinished) {
            mDrawingThread.complete();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mShouldUpdate = true;
        mDrawingThread.redraw();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_result, menu);
        
        // Set up ShareActionProvider's default share intent
        MenuItem shareItem = menu.findItem(R.id.menu_action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(getDefaultIntent());
        setShareIntent();

        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SIS_FINISHED, mFinished);
        if (mDrawingThread != null) {
            mDrawingThread.saveState(outState);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mShouldUpdate = false;
        mDrawingThread.pause();
    }
    
    /**
     * Callback Handler for CircleMeterCallback
     */
    @Override
    public void onDrawingFinished() {
        mFinished = true;
        if (mShouldUpdate) {
            setHeaderText(mCalcValue);
        }
    }
    
    @Override
    public void onProgressUpdate(double value, double degree) {
        if (mShouldUpdate) {
            int lvl = WSIKMCalculator.getLevelForDegree(degree);
            setValueText(value, lvl);
        }
    }
    
    /**
     * settings for ShareActionProvider
     */
    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_name));
        return intent;
    }
    
    private void setShareIntent() {
        int lvl = WSIKMCalculator.getLevelForValue(mCalcValue);
        String msg = getResources().getString(R.string.share_msg_value, String.format(Locale.getDefault(), "%.1f", mCalcValue)) + " ";
        if (lvl == -1) {
            msg = msg + getResources().getString(R.string.result_nolevel);
        } else if (lvl < 2) {
            msg = msg + getResources().getString(R.string.share_msg_low, getMessageForValue(mCalcValue).toLowerCase(Locale.getDefault()));
        } else if (lvl < 4) {
            msg = msg + getResources().getString(R.string.share_msg_mid, getMessageForValue(mCalcValue));
        } else if (lvl < 6) {
            msg = msg + getResources().getString(R.string.share_msg_high, getMessageForValue(mCalcValue));
        } else {
            msg = msg + mMessages[mMessages.length-1];
        }
        
        Intent intent = getDefaultIntent();
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        mShareActionProvider.setShareIntent(intent);
    }
    
    private void setHeaderText(double value) {
        mResultHeader.setText(getMessageForValue(mCalcValue));
    }
    
    private void setValueText(double value, int level) {
        String valueString;
        if (level < WSIKMCalculator.MAX_LEVEL) {
            valueString = String.format(Locale.getDefault(), "%.1f", value);
        } else {
            valueString = "\u221E";
        }
        SpannableString msg = new SpannableString(valueString + " kDoil");
        msg.setSpan(
                new ForegroundColorSpan(WSIKMCalculator.getColorForLevel(getActivity(), level)), 0,
                valueString.length(), 0);
        mResultValue.setText(msg);
    }
    
    private String getMessageForValue(double value) {
        int lvl = WSIKMCalculator.getLevelForValue(value);
        
        if (lvl == -1) {
            return getResources().getString(R.string.result_nolevel);
        } else if (lvl < mMessages.length && lvl >= 0) {
            return mMessages[lvl];
        } else {
            return mMessages[mMessages.length-1];
        }
    }
}
