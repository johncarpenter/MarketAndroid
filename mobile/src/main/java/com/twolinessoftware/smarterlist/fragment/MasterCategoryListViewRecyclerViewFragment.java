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

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.WindowManager;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnCommunicationStatusEvent;
import com.twolinessoftware.smarterlist.model.dao.MasterListItemDAO;
import com.twolinessoftware.smarterlist.service.MasterListService;
import com.twolinessoftware.smarterlist.view.MasterSmartListCategoryRecyclerViewAdapter;

import javax.inject.Inject;


public class MasterCategoryListViewRecyclerViewFragment extends BaseRecyclerViewFragment {

    private static final String EXTRA_SMART_LIST_ID = "EXTRA_SMART_LIST_ID";
    private static final String EXTRA_MASTERLIST_NAME = "EXRTA_MASTER_LIST_NAME";

    @Inject
    protected Bus m_eventBus;

    @Inject
    MasterListItemDAO m_masterListItemDAO;

    @Inject
    MasterListService m_masterListService;

    private long m_smartListId;

    private String m_masterListName;

    public static MasterCategoryListViewRecyclerViewFragment newInstance(long smartListId,String masterListName){

        Bundle args = new Bundle();
        args.putLong(EXTRA_SMART_LIST_ID,smartListId);
        args.putString(EXTRA_MASTERLIST_NAME,masterListName);

        MasterCategoryListViewRecyclerViewFragment f =  new MasterCategoryListViewRecyclerViewFragment();

        f.setArguments(args);

        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        setRetainInstance(true);
        m_smartListId = getArguments().getLong(EXTRA_SMART_LIST_ID,0);
        m_masterListName = getArguments().getString(EXTRA_MASTERLIST_NAME);
   }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.categories_pager_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        m_eventBus.register(this);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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

         m_adapter = new MasterSmartListCategoryRecyclerViewAdapter(getActivity(),m_masterListItemDAO.monitorCategoryChanges(m_masterListName),m_smartListId);

         m_masterListView.setLayoutManager(m_layoutManager);
         m_masterListView.setAdapter(m_adapter);

         setRefreshable(true);

    }

    @Override
    public void onRefresh() {
        m_masterListService.checkAndSynchronizeMasterList(m_masterListName, true);
    }

    @Subscribe
    public void onCommunicationEvent(OnCommunicationStatusEvent event) {
        switch (event.getStatus()) {
            case PROGRESS:
                setRefreshing(true);
                break;
            default:
                setRefreshing(false);
        }
    }


}
