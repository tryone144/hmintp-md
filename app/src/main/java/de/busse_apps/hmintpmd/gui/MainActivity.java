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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.busse_apps.hmintpmd.R;

public class MainActivity extends ActionBarActivity {
    
    public static final String SPLASH_FRAGMENT_TAG = "de.busse_apps.hmintpmd.gui.SplashFragment";
    public static final String INPUT_FRAGMENT_TAG = "de.busse_apps.hmintpmd.gui.InputFragment";
    public static final String RESULT_FRAGMENT_TAG = "de.busse_apps.hmintpmd.gui.ResultFragment";
    
    public static final String HELP_DIALOG_TAG = "de.busse_apps.hmintpmd.gui.HelpDialogFragment";
    public static final String ABOUT_DIALOG_TAG = "de.busse_apps.hmintpmd.gui.AboutDialogFragment";
    
    private static final String SIS_HOME_AS_UP_ENABLED = "de.busse_apps.hmintpmd.gui.MainActivity.sisHomeAsUpEnabled";
    
    private boolean homeAsUpEnabled;
    
    private FragmentManager mFragmentManager;
    private ActionBar mActionBar;
    
    private boolean cleanStart = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mActionBar = getSupportActionBar();
        mFragmentManager = getSupportFragmentManager();
        
        mFragmentManager.addOnBackStackChangedListener(new MyBackstackListener());
        
        if (savedInstanceState == null) {
            homeAsUpEnabled = false;
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            
            SplashFragment mSplashFragment = new SplashFragment();
            ft.add(R.id.main_fragment_container, mSplashFragment, SPLASH_FRAGMENT_TAG).commit();
        } else {
            cleanStart = false;
            homeAsUpEnabled = savedInstanceState.getBoolean(SIS_HOME_AS_UP_ENABLED);
            setHomeAsUpEnabled(homeAsUpEnabled);
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SIS_HOME_AS_UP_ENABLED, homeAsUpEnabled);
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_help:
                showHelp();
                return true;
            case R.id.menu_item_about:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStack();
        }
        return false;
    }
    
    private void showHelp() {
        HelpDialogFragment mHelpDialogFragment = new HelpDialogFragment();
        mHelpDialogFragment.show(mFragmentManager, HELP_DIALOG_TAG);
    }
    
    private void showAbout() {
        AboutDialogFragment mAboutDialogFragment = new AboutDialogFragment();
        mAboutDialogFragment.show(mFragmentManager, ABOUT_DIALOG_TAG);
    }
    
    public void openInputFragment() {
        InputFragment mInputFragment = new InputFragment();
        addFragment(mInputFragment, INPUT_FRAGMENT_TAG, null);
    }
    
    public void openResultFragment(double value) {
        ResultFragment resultFragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putDouble(ResultFragment.ARGUMENT_VALUE, value);
        addFragment(resultFragment, RESULT_FRAGMENT_TAG, args, true);
    }
    
    public void setHomeAsUpEnabled(boolean enabled) {
        homeAsUpEnabled = enabled;
        mActionBar.setDisplayHomeAsUpEnabled(homeAsUpEnabled);
        mActionBar.setHomeButtonEnabled(homeAsUpEnabled);
    }
    
    private void addFragment(Fragment fragment, String tag, Bundle args) {
        addFragment(fragment, tag, args, false);
    }
    
    private void addFragment(Fragment fragment, String tag, Bundle args, boolean toBackStack) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        fragment.setArguments(args);
        ft.replace(R.id.main_fragment_container, fragment, tag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (toBackStack) {
            ft.addToBackStack(tag);
        }
        ft.commit();
        
        if (toBackStack) {
            setHomeAsUpEnabled(true);
        }
    }
    
    public boolean isCleanStart() {
        return cleanStart;
    }
    
    public void setCleanStart(boolean clean) {
        cleanStart = clean;
    }
    
    /**
     * FragmentManager.OnBackStackChangedListener for handling HomeAsUp Button
     */
    private class MyBackstackListener implements FragmentManager.OnBackStackChangedListener {
        @Override
        public void onBackStackChanged() {
            boolean canback = mFragmentManager.getBackStackEntryCount() > 0;
            setHomeAsUpEnabled(canback);
        }
    }
}
