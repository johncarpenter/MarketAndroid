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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.PopupMenu;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.squareup.otto.Subscribe;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.SmarterListApplication;
import com.twolinessoftware.smarterlist.event.OnEditListSelectEvent;
import com.twolinessoftware.smarterlist.event.OnErrorEvent;
import com.twolinessoftware.smarterlist.event.OnFloatingActionButtonPressedEvent;
import com.twolinessoftware.smarterlist.event.OnInitiateSharingEvent;
import com.twolinessoftware.smarterlist.event.OnOverflowSelectedEvent;
import com.twolinessoftware.smarterlist.event.OnPlacesRequestEvent;
import com.twolinessoftware.smarterlist.event.OnPlacesSelectedEvent;
import com.twolinessoftware.smarterlist.event.OnShareManageEvent;
import com.twolinessoftware.smarterlist.event.OnShoppingListSelectEvent;
import com.twolinessoftware.smarterlist.fragment.CreateSmartListFragment;
import com.twolinessoftware.smarterlist.fragment.SharesListViewRecyclerViewFragment;
import com.twolinessoftware.smarterlist.fragment.SmartListCardViewRecyclerViewFragment;
import com.twolinessoftware.smarterlist.model.SmartList;
import com.twolinessoftware.smarterlist.service.SmartListService;
import com.twolinessoftware.smarterlist.util.AccountUtils;
import com.twolinessoftware.smarterlist.util.Ln;

import java.util.List;

import javax.inject.Inject;

import rx.schedulers.Schedulers;

public class MainNavigationActivity extends BaseNavigationActivity implements PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_PLACE_PICKER = 12342;
    private static final int REQUEST_TUTORIAL = 15325;
    private static final int REQUEST_SIGNUP = 2342;

    @Inject
    AccountUtils m_accountUtils;


    @Inject
    SmartListService m_smartListService;

    @Inject
    SharedPreferences m_preferences;

    private PopupMenu m_popUpMenu;
    private SmartList m_smartListSelected;
    private CreateSmartListFragment m_createFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkStartup();

        m_smartListService.synchronizeSmartLists();

        showFragment(SmartListCardViewRecyclerViewFragment.newInstance());

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkStartup();
    }

    private void checkStartup() {

        if ( !m_preferences.getBoolean(Constants.PREFERENCE_SHOWN_TUTORIAL,false)) {
            Ln.v("Launching Tutorial");
            startActivityForResult(new Intent(this, TutorialActivity.class), REQUEST_TUTORIAL);
        } else if (!m_accountUtils.isLoggedIn() ){
            startActivityForResult(new Intent(this,LoginActivity.class),REQUEST_SIGNUP);
            Ln.v("Launching Login");
        }else{
            Ln.v("Launching Main App");
            checkForSharingIntent(getIntent());
            m_smartListService.synchronizeSmartLists();
        }

    }

    private void checkForSharingIntent(Intent intent) {

        Ln.v("Checking for share intent");

        if(intent != null){
            final String action = intent.getAction();

            Ln.v("Intent action:"+action);

            if (Intent.ACTION_VIEW.equals(action) || action.equalsIgnoreCase("android.nfc.action.NDEF_DISCOVERED")) {
                Ln.v("Launching from view");
                final List<String> segments = intent.getData().getPathSegments();
                if (segments.size() > 1) {
                    String shareToken = segments.get(3);
                    Ln.v("Subscribing via token:"+shareToken);
                    m_smartListService.subscribeToShareToken(shareToken)
                            .subscribeOn(Schedulers.io())
                            .subscribe(apiResponse-> m_smartListService.synchronizeSmartLists(),error-> showErrorCrouton(getString(R.string.error_invalid_share_token)));
                }
            }


        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setTitle(R.string.main_title);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((SmarterListApplication)getApplication()).performShutdown();
    }

    @Subscribe
    public void onNavigateToShoppingView(OnShoppingListSelectEvent event){

        Ln.d("Launching new shopping view:" + event.getSmartList().getName());

       // ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainNavigationActivity.this, event.getTransitionView(), ShoppingNavigationActivity.IMAGE_TRANSITION_NAME);
        Intent intent = new Intent(MainNavigationActivity.this, ShoppingNavigationActivity.class);
        intent.putExtra(ShoppingNavigationActivity.EXTRA_SMART_LIST,event.getSmartList());
        ActivityCompat.startActivity(MainNavigationActivity.this, intent, null);

    }

    @Subscribe
    public void onNavigateToEditView(OnEditListSelectEvent event){

        Ln.d("Launching new edit view:" + event.getSmartList().getName());

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainNavigationActivity.this, event.getTransitionView(), PlanViewPagerActivity.IMAGE_TRANSITION_NAME);
        Intent intent = new Intent(MainNavigationActivity.this, PlanViewPagerActivity.class);
        intent.putExtra(PlanViewPagerActivity.EXTRA_SMART_LIST,event.getSmartList());
        ActivityCompat.startActivity(MainNavigationActivity.this, intent, null);

    }

    @Subscribe
    public void onOverflowActionSelected(OnOverflowSelectedEvent event){

        Ln.v("Showing Action Menu for "+event.getSmartList().getName());

        m_smartListSelected = event.getSmartList();

        m_popUpMenu = new PopupMenu(MainNavigationActivity.this,event.getAnchorView());
        if(m_accountUtils.isOwner(m_smartListSelected)){
            m_popUpMenu.inflate(R.menu.popup_smartlist);
        }else{
            m_popUpMenu.inflate(R.menu.popup_smartlist_shared);
        }


        m_popUpMenu.setOnMenuItemClickListener(this);
        m_popUpMenu.show();

    }


    @Subscribe
    public void onFloatingActionButtonPressed(OnFloatingActionButtonPressedEvent event){
        m_createFragment =CreateSmartListFragment.newInstance(0);
        showFragment(m_createFragment,true,false);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_share:

                generateShare(m_smartListSelected);

                return true;
            case R.id.menu_delete:
                Dialog dialog = new AlertDialog.Builder(MainNavigationActivity.this)
                        .setTitle(getString(R.string.card_delete_action_title))
                        .setMessage(getString(R.string.card_delete_action_description))
                        .setPositiveButton(R.string.dialog_delete, (dialog1, which) -> {
                            if(m_smartListSelected != null){
                                m_smartListService.deleteSmartList(m_smartListSelected);
                            }
                        })
                        .setCancelable(true)
                        .setNegativeButton(R.string.dialog_cancel, (dialog1, which) -> {
                            dialog1.dismiss();
                        })
                        .create();

                dialog.show();
                m_popUpMenu.dismiss();
                return true;
            case R.id.menu_edit:
                m_createFragment =CreateSmartListFragment.newInstance(m_smartListSelected.getItemId());
                showFragment(m_createFragment,true,false);
                return true;

            case R.id.menu_leave:
                m_smartListService.removeSharing(m_smartListSelected);
                return true;


        }

        return false;
    }

    private void generateShare(SmartList smartList) {
        showLoadingDialog(getString(R.string.dialog_generate_token));

        m_smartListService.generateShareToken(smartList)
                .subscribeOn(Schedulers.io())
                .subscribe(shareToken -> {

                    dismissDialog();

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
                    i.putExtra(Intent.EXTRA_TEXT, shareToken.getUrl());
                    startActivity(Intent.createChooser(i, "Share Shopping List Via"));

                }, error -> {
                    dismissDialog();
                    showErrorCrouton(getString(R.string.error_communication_generic));
                });
    }

    @Subscribe
    public void onErrorEvent(OnErrorEvent event) {
        if(event.getError() != OnErrorEvent.Error.AUTHENTICATION) {
            showErrorCrouton(getString(event.getError().getDisplayError()));
        }
    }

    @Subscribe
    public void onPlacesRequestEvent(OnPlacesRequestEvent event){
        // Construct an intent for the place picker
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the intent by requesting a result,
            // identified by a request code.
            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            Ln.e("Repairable Play Services error:"+e.getConnectionStatusCode());
            onErrorEvent(new OnErrorEvent(OnErrorEvent.Error.GOOGLE_SERVICES));
        } catch (GooglePlayServicesNotAvailableException e) {
            onErrorEvent(new OnErrorEvent(OnErrorEvent.Error.GOOGLE_SERVICES));
        }
    }

    @Subscribe
    public void onShareManageEvent(OnShareManageEvent event){
        showFragment(SharesListViewRecyclerViewFragment.newInstance(event.getSmartListId()),true,false);
    }

    @Subscribe
    public void onInitiateSharingEvent(OnInitiateSharingEvent event){
        generateShare(event.getSmartList());
    }


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if(resultCode == RESULT_CANCELED){
                finish();
            }else{
                checkStartup();
            }
        }if (requestCode == REQUEST_TUTORIAL){
            m_preferences.edit().putBoolean(Constants.PREFERENCE_SHOWN_TUTORIAL,true).commit();
            checkStartup();
        } else if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                if(m_createFragment != null)
                    m_createFragment.onPlacesSelected(new OnPlacesSelectedEvent(place,PlacePicker.getAttributions(data)));
               // m_eventBus.post(new OnPlacesSelectedEvent(place,PlacePicker.getAttributions(data)));
            }
        }
    }

}
