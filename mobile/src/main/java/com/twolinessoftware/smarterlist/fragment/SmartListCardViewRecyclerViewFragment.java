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
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnCommunicationStatusEvent;
import com.twolinessoftware.smarterlist.event.OnErrorEvent;
import com.twolinessoftware.smarterlist.model.dao.SmartListDAO;
import com.twolinessoftware.smarterlist.service.SmartListService;
import com.twolinessoftware.smarterlist.view.SmartListCardRecyclerViewAdapter;

import javax.inject.Inject;


public class SmartListCardViewRecyclerViewFragment extends BaseRecyclerViewFragment {

    @Inject
    protected Bus m_eventBus;

    @Inject
    SmartListDAO m_smartListDao;



    public static SmartListCardViewRecyclerViewFragment newInstance(){

        Bundle args = new Bundle();

        SmartListCardViewRecyclerViewFragment f =  new SmartListCardViewRecyclerViewFragment();
        f.setArguments(args);

        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
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
        super.onViewCreated(view,savedInstanceState);

        m_layoutManager = new GridLayoutManager(getActivity(),getSpanCount());

        m_adapter = new SmartListCardRecyclerViewAdapter(getActivity(),m_smartListDao.monitorSmartListChanges());


        m_masterListView.setLayoutManager(m_layoutManager);
        m_masterListView.setAdapter(m_adapter);


        getBaseActivity().setFloatingActionButtonVisibility(true);
         getToolbar().setTitle(R.string.main_title);
         setRefreshable(true);

    }


    /**
     * Refresh needs to sync with the server.
     *
     * @todo adding the server callbacks
     */

    @Inject
    SmartListService m_smartListService;

    @Override
    public void onRefresh() {
        m_smartListService.synchronizeSmartLists();
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

    @Subscribe
    public void onErrorEvent(OnErrorEvent event) {
        setRefreshing(false);
    }

}
