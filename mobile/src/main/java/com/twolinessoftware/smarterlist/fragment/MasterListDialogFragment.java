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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnDialogCancelEvent;
import com.twolinessoftware.smarterlist.event.OnSmartListSelectedEvent;
import com.twolinessoftware.smarterlist.model.dao.MasterSmartListDAO;
import com.twolinessoftware.smarterlist.view.MasterSmartListRecyclerViewAdapter;

import javax.inject.Inject;

import butterknife.InjectView;


public class MasterListDialogFragment extends BaseDialogFragment {

    @Inject
    protected Bus m_eventBus;

    @Inject
    MasterSmartListDAO m_masterSmartListDAO;

    @InjectView(R.id.list_master)
    RecyclerView m_masterListView;

    @InjectView(R.id.toolbar)
    Toolbar m_toolbar;

    protected RecyclerView.Adapter m_adapter;

    protected RecyclerView.LayoutManager m_layoutManager;

    @Override
    protected final int setContentView() {
        return R.layout.dialog_select_masterlistview;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        setHasOptionsMenu(true);

        setStyle(STYLE_NO_FRAME, R.style.SmarterList);
    }

    @Override
    public void onResume() {
        super.onResume();
        m_eventBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        m_eventBus.unregister(this);
    }

     @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);

         m_layoutManager = new LinearLayoutManager(getActivity());

         m_adapter = new MasterSmartListRecyclerViewAdapter(getActivity(),m_masterSmartListDAO.monitorMasterSmartLists());

         m_masterListView.setLayoutManager(m_layoutManager);
         m_masterListView.setAdapter(m_adapter);

         m_toolbar.setTitle(R.string.dialog_select_title);
         getBaseActivity().setSupportActionBar(m_toolbar);
    }


    @Subscribe
    public void onSmartListSelected(OnSmartListSelectedEvent event){
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        m_eventBus.post(new OnDialogCancelEvent());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.create_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_save:

                dismiss();
                return true;
            default:
                return false;
        }
    }
}
