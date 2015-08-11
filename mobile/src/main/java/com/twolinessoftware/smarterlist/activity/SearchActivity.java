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

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import com.squareup.otto.Subscribe;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnAddNewMasterListItemEvent;
import com.twolinessoftware.smarterlist.event.OnListItemSelectedEvent;
import com.twolinessoftware.smarterlist.event.OnMasterListItemAddedEvent;
import com.twolinessoftware.smarterlist.fragment.AddMasterListItemFragment;
import com.twolinessoftware.smarterlist.fragment.MasterListSearchRecyclerViewFragment;
import com.twolinessoftware.smarterlist.model.MasterSmartListItem;
import com.twolinessoftware.smarterlist.model.SmartList;
import com.twolinessoftware.smarterlist.model.SmartListItem;
import com.twolinessoftware.smarterlist.model.dao.SmartListItemDAO;
import com.twolinessoftware.smarterlist.util.Ln;

import javax.inject.Inject;


public class SearchActivity extends BaseActivity {

    public static final String EXTRA_SMART_LIST = "EXTRA_SMART_LIST";
    public static final String IMAGE_TRANSITION_NAME = "image_transition_name";

    @Inject
    SmartListItemDAO m_smartListItemDao;

    private SmartList m_smartList;

    private MasterListSearchRecyclerViewFragment m_searchFragment;

    private AddMasterListItemFragment m_addItemfragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateView(getIntent());
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_search;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateView(intent);
    }

    private void updateView(Intent intent) {
        m_smartList = intent.getParcelableExtra(EXTRA_SMART_LIST);
        m_searchFragment = MasterListSearchRecyclerViewFragment.newInstance(Constants.DEFAULT_MASTERLIST_NAME,0 , (m_smartList != null)?m_smartList.getItemId():0);

        showFragment(m_searchFragment);
    }

    private void handleSearchIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Ln.v("New Search Query from Intent:" + query);
        }
    }

    @Subscribe
    public void onAddNewItem(OnAddNewMasterListItemEvent event){
        m_addItemfragment = AddMasterListItemFragment.newInstance(event.getMasterListName(),event.getLastSearchQuery());
        showFragment(m_addItemfragment, true);
    }

    @Subscribe
    public void onListItemSelected(OnListItemSelectedEvent event){
        Ln.v("Toggling Item:"+event.getItem().getName());

        MasterSmartListItem masterSmartListItem = event.getItem();

        SmartListItem item = new SmartListItem(masterSmartListItem,null,0,m_smartList.getItemId());
        m_smartListItemDao.toggleIncluded(item);

    }

    @Subscribe
    public void onNewItemAdded(OnMasterListItemAddedEvent event){
       // if(m_smartList != null){
            Ln.v("Adding the new item to the smartlist");
            SmartListItem item = new SmartListItem(event.getMasterSmartListItem(),null,0,m_smartList.getItemId());
            m_smartListItemDao.toggleIncluded(item);
      //  }

    }



}
