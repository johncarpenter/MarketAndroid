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

package com.twolinessoftware.smarterlist.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnInitiateSharingEvent;
import com.twolinessoftware.smarterlist.event.OnPlacesRequestEvent;
import com.twolinessoftware.smarterlist.event.OnPlacesSelectedEvent;
import com.twolinessoftware.smarterlist.event.OnShareManageEvent;
import com.twolinessoftware.smarterlist.model.MasterSmartList;
import com.twolinessoftware.smarterlist.model.ShareGroup;
import com.twolinessoftware.smarterlist.model.SmartList;
import com.twolinessoftware.smarterlist.model.dao.MasterSmartListDAO;
import com.twolinessoftware.smarterlist.model.dao.SmartListDAO;
import com.twolinessoftware.smarterlist.service.SmartListService;
import com.twolinessoftware.smarterlist.util.AccountUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by John on 2015-03-30.
 */
public class CreateSmartListFragment extends BaseFragment {

    private static final String EXTRA_SMARTLIST_ID = "EXTRA_SMARTLIST_ID";

    @InjectView(R.id.edit_smartlist_name)
    EditText m_editSmartListName;

    @InjectView(R.id.edit_smartlist_description)
    EditText m_editSmartListDescription;

    @InjectView(R.id.text_places_name)
    TextView m_textPlacesName;

    @InjectView(R.id.text_places_name_sub)
    TextView m_textPlacesNameSub;

    @InjectView(R.id.text_sharing_name)
    TextView m_textSharingName;

    @InjectView(R.id.layout_select_sharing)
    ViewGroup m_layoutSharing;

    @InjectView(R.id.text_select_sharing)
    TextView m_textSharingCaption;


    @Inject
    SmartListService m_smartListService;

    @Inject
    SmartListDAO m_smartListDAO;

    @Inject
    MasterSmartListDAO m_masterSmartListDAO;

    @Inject
    AccountUtils m_accountUtils;

    @Inject
    protected Bus m_eventBus;

    private MasterSmartList m_selectedSmartList;

    private SmartList m_smartListEdit;

    private List<ShareGroup> m_shareGroupList;

    public static CreateSmartListFragment newInstance(long smartListId){

        CreateSmartListFragment f =  new CreateSmartListFragment();

        Bundle args = new Bundle();
        args.putLong(EXTRA_SMARTLIST_ID, smartListId);

        f.setArguments(args);

        return f;

    }




    @Override
    protected int setContentView() {
        return R.layout.fragment_create_smartlist;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);

        long smartListId = getArguments().getLong(EXTRA_SMARTLIST_ID);
        if(smartListId != 0){
            m_smartListEdit = m_smartListDAO.findByItemId(smartListId);
        }


    }

    private void prepopulate() {

        m_editSmartListName.setText(m_smartListEdit.getName());
        m_editSmartListDescription.setText(m_smartListEdit.getDescription());

        if(m_accountUtils.isOwner(m_smartListEdit)) {
            m_layoutSharing.setVisibility(View.VISIBLE);
            m_textSharingCaption.setVisibility(View.VISIBLE);
            preloadSharing();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        m_eventBus.register(this);
        m_removeFromStack = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        m_eventBus.unregister(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        getBaseActivity().setFloatingActionButtonVisibility(false);

        configureToolbar();
        m_selectedSmartList = m_masterSmartListDAO.findByName(Constants.DEFAULT_MASTERLIST_NAME);

        if(m_smartListEdit != null){
            prepopulate();
        }else{
            setDefaultName();
        }
    }

    @OnClick(R.id.layout_select_places)
    public void onSelectPlacesClick(View view){
        m_eventBus.post(new OnPlacesRequestEvent());
        m_removeFromStack = false;
    }

    @OnClick(R.id.layout_select_sharing)
    public void onSharingClick(View view){

            if(m_shareGroupList.size() == 0){
                m_eventBus.post(new OnInitiateSharingEvent(m_smartListEdit));
            }else{
                m_eventBus.post(new OnShareManageEvent(m_smartListEdit.getItemId()));
            }


            m_removeFromStack = false;
    }

    private void setDefaultName() {
        Date date = new Date();
        String day = new SimpleDateFormat("MMM dd", Locale.US).format(date);
        m_editSmartListName.setText(getString(R.string.create_default_name,day));
    }

    private void createAndClose() {

        if(m_smartListEdit == null){

            // New SmartList

            SmartList smartList = new SmartList(m_editSmartListName.getText().toString().trim(),
                    m_editSmartListDescription.getText().toString().trim(),
                    m_selectedSmartList.getName(),m_selectedSmartList.getIconUrl());

            // Write the data, sync later
            smartList.setItemId(0);
            int localId = m_smartListDAO.save(smartList);
            smartList = m_smartListDAO.findById(localId);

            // Try a background sync now
            m_smartListService.createSmartList(smartList);
        }else{

            // Edited SmartList
            String editListName = m_editSmartListName.getText().toString().trim();
            String editListDescription = m_editSmartListDescription.getText().toString().trim();

            m_smartListEdit.setName(editListName);
            m_smartListEdit.setDescription(editListDescription);
            m_smartListEdit.setLastModified(new Date());

            m_smartListDAO.save(m_smartListEdit);

            m_smartListService.synchronizeSmartLists();

        }

        finish(true);
    }

    private void preloadSharing(){
        if(m_smartListEdit != null) {

            AndroidObservable.bindFragment(this,
            m_smartListService.getListOfShares(m_smartListEdit.getItemId()))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(shares -> {
                        this.m_shareGroupList = shares;
                        int shareCount = shares.size();
                        m_textSharingName.setText(getResources().getQuantityString(R.plurals.create_shared_with, shareCount, shareCount));
                    },error->{
                        m_smartListService.handleErrors(error);
                    });

        }

    }

    @Subscribe
    public void onPlacesSelected(OnPlacesSelectedEvent event){
        m_textPlacesName.setText(event.getPlace().getName());

        if(event.getAttributions() != null){
            m_textPlacesNameSub.setText(Html.fromHtml(event.getAttributions()));
        }

    }

    private void configureToolbar() {
        startActionMode(m_ActionModeCallback);
        getActionMode().setTitle(R.string.create_title);
    }


    private boolean m_removeFromStack = true;
    private ActionMode.Callback m_ActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.create_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_save:
                    createAndClose();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            finish(m_removeFromStack);
        }
    };


}
