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
import android.support.v7.widget.LinearLayoutManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.service.SmartListService;
import com.twolinessoftware.smarterlist.view.SharesListRecyclerViewAdapter;

import javax.inject.Inject;


public class SharesListViewRecyclerViewFragment extends BaseRecyclerViewFragment  {

    private static final String EXTRA_SMARTLIST_ID = "EXTRA_SMARTLIST_ID";
    @Inject
    protected Bus m_eventBus;

    @Inject
    SmartListService m_smartListService;

    private long m_smartListId;


    public static SharesListViewRecyclerViewFragment newInstance(long smartListId){

        Bundle args = new Bundle();

        args.putLong(EXTRA_SMARTLIST_ID,smartListId);
        SharesListViewRecyclerViewFragment f =  new SharesListViewRecyclerViewFragment();
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        setHasOptionsMenu(true);
        m_smartListId = getArguments().getLong(EXTRA_SMARTLIST_ID);

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

        m_adapter = new SharesListRecyclerViewAdapter(getActivity(),m_smartListService.getListOfShares(m_smartListId));

        m_masterListView.setLayoutManager(m_layoutManager);
        m_masterListView.setAdapter(m_adapter);

        getToolbar().setTitle(R.string.contacts_search_title);
        getToolbar().setBackgroundColor(getResources().getColor(R.color.pal_4));

        configureToolbar();
   }

    private void configureToolbar() {

        startActionMode(m_actionModeCallback);
        getActionMode().setTitle(R.string.contacts_search_title);
    }

    private ActionMode.Callback m_actionModeCallback = new ActionMode.Callback() {

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
                    //save();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            finish(true);
        }
    };
}
