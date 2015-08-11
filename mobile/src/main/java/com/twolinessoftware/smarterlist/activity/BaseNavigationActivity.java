/*
 * Copyright (c) 2015. 2Lines Software,Inc
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

package com.twolinessoftware.smarterlist.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.twolinessoftware.smarterlist.BuildConfig;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnLogoutEvent;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Base functionality for all Activities. Injects butterknife and eventbus.
 *
 */
@SuppressLint("Registered")
public class BaseNavigationActivity extends BaseActivity {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    @InjectView(R.id.drawer_layout)
    DrawerLayout m_drawerLayout;

    @InjectView(R.id.text_nav_email)
    TextView m_textNavEmail;

    @InjectView(R.id.navigation_drawer)
    NavigationView m_navigationView;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        setupNavigationDrawer();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_drawer;
    }

    private void setupNavigationDrawer() {

        ActionBarDrawerToggle m_drawerToggle = new ActionBarDrawerToggle(
                this,
                m_drawerLayout,
                m_toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        m_drawerLayout.setDrawerListener(m_drawerToggle);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        m_drawerToggle.syncState();

        m_textNavEmail.setText(m_accountUtils.getEmailAddress());

        m_navigationView.setNavigationItemSelectedListener(menuItem -> {

            switch(menuItem.getItemId()){
                case R.id.menu_about:
                    showAboutDialog();
                    return true;
                case R.id.menu_feedback:
                    sendFeedbackEmail();
                    return true;
            }

            return false;
        });
    }

    private void showAboutDialog() {
        Dialog dialog = new AlertDialog.Builder(BaseNavigationActivity.this)
                .setTitle(getString(R.string.dialog_about_title))
                .setMessage(Html.fromHtml(getString(R.string.dialog_about_text, BuildConfig.VERSION_NAME)))
                .setPositiveButton(R.string.dialog_close, (dialog1, which) -> {
                    dialog1.dismiss();

                })
                .setCancelable(true)
                .create();

        dialog.show();
    }

    private void sendFeedbackEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "feedback@smarterlistapp.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        startActivity(Intent.createChooser(emailIntent, getString(R.string.email_send_text)));

    }

    @Override
    public void onBackPressed() {

        if (m_drawerLayout.isDrawerOpen(GravityCompat.START)) {
            m_drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.text_nav_email)
    public void onLogoutClick(View view){

        Dialog dialog = new AlertDialog.Builder(BaseNavigationActivity.this)
                .setTitle(getString(R.string.dialog_logout_title))
                .setMessage(getString(R.string.dialog_logout_text))
                .setPositiveButton(R.string.dialog_logout, (dialog1, which) -> {
                    m_eventBus.post(new OnLogoutEvent());
                    m_drawerLayout.closeDrawers();

                })
                .setCancelable(true)
                .setNegativeButton(R.string.dialog_cancel, (dialog1, which) -> {
                    dialog1.dismiss();
                })
                .create();

        dialog.show();
    }

}
