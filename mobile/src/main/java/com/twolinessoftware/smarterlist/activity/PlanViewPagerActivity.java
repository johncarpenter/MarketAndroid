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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.SearchView;
import android.view.View;

import com.squareup.otto.Subscribe;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnListItemSelectedEvent;
import com.twolinessoftware.smarterlist.event.OnNavigateToCategory;
import com.twolinessoftware.smarterlist.event.OnNotesIconSelectedEvent;
import com.twolinessoftware.smarterlist.fragment.MasterCategoryListViewRecyclerViewFragment;
import com.twolinessoftware.smarterlist.fragment.MasterListPredictedRecyclerViewFragment;
import com.twolinessoftware.smarterlist.fragment.MasterListViewRecyclerViewFragment;
import com.twolinessoftware.smarterlist.fragment.NotesEntryDialogFragment;
import com.twolinessoftware.smarterlist.model.MasterSmartListItem;
import com.twolinessoftware.smarterlist.model.SmartList;
import com.twolinessoftware.smarterlist.model.SmartListItem;
import com.twolinessoftware.smarterlist.model.dao.SmartListItemDAO;
import com.twolinessoftware.smarterlist.service.MasterListService;
import com.twolinessoftware.smarterlist.util.Ln;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;


public class PlanViewPagerActivity extends BaseViewPagerActivity {

    public static final String EXTRA_SMART_LIST = "EXTRA_SMART_LIST";
    public static final String IMAGE_TRANSITION_NAME = "IMAGE_TRANSITION_NAME";

    @Inject
    MasterListService m_masterListService;

    @Inject
    SmartListItemDAO m_smartListItemDao;

    private SmartList m_smartList;

    @InjectView(R.id.search_bar)
    View m_searchView;


    private MasterListViewRecyclerViewFragment m_categoryListItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_smartList = getIntent().getParcelableExtra(EXTRA_SMART_LIST);

        setTitle(m_smartList.getName());

        // My List
        //addFragment(SmartItemListViewRecyclerViewFragment.newInstance(m_smartList, SmartListItemRecyclerViewAdapter.SelectionMode.Delete));

        // Category View
        addFragment(MasterCategoryListViewRecyclerViewFragment.newInstance(m_smartList.getItemId(), Constants.DEFAULT_MASTERLIST_NAME));

        // Category Item VIew
        m_categoryListItem = MasterListViewRecyclerViewFragment.newInstance(Constants.DEFAULT_MASTERLIST_NAME,0,m_smartList.getItemId());
        addFragment(m_categoryListItem);

        // Predicted List View
        addFragment(MasterListPredictedRecyclerViewFragment.newInstance(Constants.DEFAULT_MASTERLIST_NAME, m_smartList.getItemId()));

        refresh();

        //ViewCompat.setTransitionName(m_pager,IMAGE_TRANSITION_NAME);
        m_pager.setCurrentItem(0,true);

        // Verify the list has been synced
        // verifyAndDownloadIfNecessary();

        setFloatingActionButtonVisibility(false);

        SearchView searchView = (SearchView) m_searchView.findViewById(R.id.search_view);
        searchView.setFocusable(false);
        searchView.setOnClickListener(v -> launchSearch());
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                launchSearch();
                searchView.clearFocus();
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void launchSearch() {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(PlanViewPagerActivity.this, m_searchView, SearchActivity.IMAGE_TRANSITION_NAME);
        Intent intent = new Intent(PlanViewPagerActivity.this, SearchActivity.class);
        intent.putExtra(PlanViewPagerActivity.EXTRA_SMART_LIST,m_smartList);
        ActivityCompat.startActivity(PlanViewPagerActivity.this, intent, options.toBundle());
    }


    private void verifyAndDownloadIfNecessary() {
        m_masterListService.checkAndSynchronizeMasterList(m_smartList.getMasterListName(),false);
    }

    @Override
    public void onBackPressed() {
        int currentItem = m_pager.getCurrentItem();
        if(currentItem != 0){
            m_pager.setCurrentItem(0,true);
        }else{
            super.onBackPressed();
        }
    }

    @OnClick(R.id.search_bar)
    public void clickSearch(View view){
       launchSearch();
    }

    @Subscribe
    public void onNavigationToNewCategoryEvent(OnNavigateToCategory event){
        m_categoryListItem.setCategory(event.getCategory());
        m_pager.setCurrentItem(1,true);
     }



    @Subscribe
    public void onListItemSelected(OnListItemSelectedEvent event){
        Ln.v("Toggling Item:"+event.getItem().getName());

        MasterSmartListItem masterSmartListItem = event.getItem();

        SmartListItem item = new SmartListItem(masterSmartListItem,null,0,m_smartList.getItemId());
        m_smartListItemDao.toggleIncluded(item);

        m_accountUtils.scheduleSmartlistSync();

    }

    @Subscribe
    public void onNotesIconSelected(OnNotesIconSelectedEvent event){
        showDialogFragment(NotesEntryDialogFragment.newInstance(event.getSmartListItem().getItemId()));
    }



 }

