/*
 * Copyright (c) 2015. 2Lines Software,Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.twolinessoftware.smarterlist.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnFloatingActionButtonPressedEvent;
import com.twolinessoftware.smarterlist.fragment.BaseFragment;
import com.twolinessoftware.smarterlist.fragment.LoadingDialogFragment;
import com.twolinessoftware.smarterlist.util.AccountUtils;
import com.twolinessoftware.smarterlist.util.Ln;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Base functionality for all Activities. Injects butterknife and eventbus.
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {


  //  @Optional
    @InjectView(R.id.toolbar)
    Toolbar m_toolbar;

    protected ActionMode m_actionMode;


    @Optional
    @InjectView(R.id.button_float)
    FloatingActionButton m_floatingActionButton;

    @Optional
    @InjectView(R.id.progress_bar)
    ProgressBar m_progressBar;


    @Inject
    protected Bus m_eventBus;

    //@Inject
    //protected AnalyticsUtil m_analyticsUtils;

    @Inject
    AccountUtils m_accountUtils;




    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(getContentView());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        Injector.inject(this);
    }

    protected int getContentView() {
        return R.layout.activity_main;
    }

    public void setFloatingActionButtonVisibility(boolean visible) {
        m_floatingActionButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.button_float)
    public void onFloatingActionButtonPressed(View view) {
        m_eventBus.post(new OnFloatingActionButtonPressedEvent());
    }

    public void setContentView(final int layoutResId) {
        super.setContentView(layoutResId);

        ButterKnife.inject(this);

        if (m_toolbar != null) {
            setSupportActionBar(m_toolbar);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_eventBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        m_eventBus.unregister(this);
        Crouton.cancelAllCroutons();
        dismissDialog();
    }

    public Toolbar getToolbar() {
        return m_toolbar;
    }

    public ActionMode getActionMode() {
        return m_actionMode;
    }

    public void showFragment(BaseFragment fragment, boolean addToBack, boolean showAnimation) {
        Ln.v("Showing fragment:" + fragment.getClass().getSimpleName());

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (addToBack) {
            ft.addToBackStack(null);
        }
        if (showAnimation) {
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
        }

        ft.replace(R.id.container, fragment).commit();
    }

    protected void showFragment(BaseFragment fragment) {
        showFragment(fragment, false, true);
    }

    public void showFragment(BaseFragment fragment, boolean addToBack) {
        showFragment(fragment, addToBack, true);
    }


    public void showErrorCrouton(String message) {

        Style style = new Style.Builder()
                .setHeightDimensionResId(R.dimen.crouton_height)
                .setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)
                .setBackgroundColorValue(getResources().getColor(R.color.pal_red))
                .setTextAppearance(R.style.SmarterList_TextStyle_Body_Bold)
                .build();

        Crouton.makeText(this, message, style, R.id.container).show();
    }

    public Crouton getInfoCrouton(String message) {

        Style style = new Style.Builder()
                .setHeightDimensionResId(R.dimen.crouton_height)
                .setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)
                .setBackgroundColorValue(getResources().getColor(R.color.pal_blue))
                .setTextAppearance(R.style.SmarterList_TextStyle_Body_Bold)
                .build();

        return Crouton.makeText(this, message, style, R.id.container);
    }


    public void showProgress(boolean visible) {

        m_progressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }


    public void clearBackStack() {
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            int backStackId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
            getSupportFragmentManager().popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void showDialogFragment(DialogFragment fragment) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        fragment.show(ft, "dialog");
    }

    public void showLoadingDialog(String message) {
        showDialogFragment(LoadingDialogFragment.newInstance(message));
    }

    public void dismissDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.commit();
    }

    public void configureActionMode(ActionMode.Callback callback) {
        m_actionMode = getToolbar().startActionMode(callback);
    }

    public void resetActionMode() {

        if (m_actionMode != null) {
            m_actionMode.finish();
            m_actionMode = null;
        }

    }

    @Override
    public void onBackPressed() {
        resetActionMode();

        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }



}
