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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.busse_apps.hmintpmd.R;
import de.busse_apps.hmintpmd.widget.CircleMeterDrawingThread;
import de.busse_apps.hmintpmd.widget.CircleMeterView;

public class SplashFragment extends Fragment implements CircleMeterView.CircleMeterCallback {

    private static final String ERROR_DIALOG_TAG = "de.busse_apps.hmintpmd.gui.ErrorDialogFragment";

    private static final String SIS_SHOULD_FAIL = "de.busse_apps.hmintpmd.gui.SplashFragment.shouldFail";
    private static final String SIS_FINISHED = "de.busse_apps.hmintpmd.gui.SplashFragment.finished";
    private static final String SIS_FAILED = "de.busse_apps.hmintpmd.gui.SplashFragment.failed";

    private static final int NO_FAIL = -1;

    private MainActivity mActivity;
    private TextView mBannerView;
    private Button mButtonStart;

    private CircleMeterView mProgressGraph;
    private CircleMeterDrawingThread mDrawingThread;

    private boolean mShouldUpdate;
    private int mShouldFail = NO_FAIL;

    private boolean mFinished = false;
    private boolean mFailed = false;

    private String[] mSplashMessages;
    private String[] mErrorTitles;
    private String[] mErrorMessages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mShouldFail = savedInstanceState.getInt(SIS_SHOULD_FAIL);
        } else {
            mShouldFail = generateFailAt();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();

        mBannerView = (TextView) getView().findViewById(R.id.splash_banner_tv);

        mButtonStart = (Button) getView().findViewById(R.id.splash_button_start);
        mButtonStart.setOnClickListener(new onButtonStartClickListener());

        mProgressGraph = (CircleMeterView) getView().findViewById(R.id.splash_progress_graph);
        mProgressGraph.setMaxValue(5);
        mProgressGraph.setMaxDegree(CircleMeterView.MAX_DEGREE);
        mProgressGraph.setCallback(this);

        mDrawingThread = mProgressGraph.getThread();

        if (savedInstanceState != null) {
            mDrawingThread.restoreState(savedInstanceState);
            mFinished = savedInstanceState.getBoolean(SIS_FINISHED);
            mFailed = savedInstanceState.getBoolean(SIS_FAILED);
        }
        if (mShouldFail != NO_FAIL) {
            mSplashMessages = getResources().getStringArray(R.array.splash_messages);
            mErrorTitles = getResources().getStringArray(R.array.dialog_error_titles);
            mErrorMessages = getResources().getStringArray(R.array.dialog_error_messages);
        }
        if (mFinished) {
            mDrawingThread.complete();
        }
        if (mFailed) {
            mDrawingThread.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mFailed) {
            mShouldUpdate = true;
            mDrawingThread.redraw();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SIS_SHOULD_FAIL, mShouldFail);
        outState.putBoolean(SIS_FINISHED, mFinished);
        outState.putBoolean(SIS_FAILED, mFailed);
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
            mBannerView.setVisibility(View.INVISIBLE);
            mButtonStart.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onProgressUpdate(double stage, double degree) {
        if (mShouldUpdate) {
            mBannerView.setText(getSplashMessageForStage((int) stage));
            if (mShouldFail == (int) stage) {
                mDrawingThread.pause();
                mShouldUpdate = false;
                if (!mFailed) {
                    showErrorDialog((int) stage);
                    mFailed = true;
                }
            }
        }
    }

    private void showErrorDialog(int stage) {
        if (getFragmentManager().findFragmentByTag(ERROR_DIALOG_TAG) == null) {
            Bundle args = new Bundle();
            args.putString(ErrorDialogFragment.ARG_TITLE, getErrorTitleForStage(stage));
            args.putString(ErrorDialogFragment.ARG_MESSAGE, getErrorMessageForStage(stage));

            ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
            errorDialogFragment.setArguments(args);
            errorDialogFragment.setCancelable(false);
            errorDialogFragment.show(getFragmentManager(), ERROR_DIALOG_TAG);
        }
    }

    private String getSplashMessageForStage(int stage) {
        if (stage < mSplashMessages.length && stage >= 0) {
            return mSplashMessages[stage];
        } else {
            return getResources().getString(R.string.splash_banner_start);
        }
    }

    private String getErrorTitleForStage(int stage) {
        if (stage < mErrorTitles.length && stage >= 0) {
            return mErrorTitles[stage];
        } else {
            return getResources().getString(R.string.dialog_error_title);
        }
    }

    private String getErrorMessageForStage(int stage) {
        if (stage < mErrorMessages.length && stage >= 0) {
            return mErrorMessages[stage];
        } else {
            return "";
        }
    }

    private int generateFailAt() {
        boolean possible = (int) (Math.random() * 3) == 0;
        if (possible) {
            return (int) (Math.random() * 4);
        }
        return NO_FAIL;
    }

    /**
     * View.onClickListener for Start Button
     */
    private class onButtonStartClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mActivity.openInputFragment();
        }
    }
}
