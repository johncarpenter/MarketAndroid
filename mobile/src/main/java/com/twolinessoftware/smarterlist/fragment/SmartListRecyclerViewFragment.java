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
import android.view.View;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.model.dao.SmartListDAO;
import com.twolinessoftware.smarterlist.util.Ln;
import com.twolinessoftware.smarterlist.view.SmartListRecyclerViewAdapter;

import javax.inject.Inject;


public class SmartListRecyclerViewFragment extends BaseRecyclerViewFragment {

    @Inject
    protected Bus m_eventBus;

    @Inject
    SmartListDAO m_smartListDao;

    public static SmartListRecyclerViewFragment newInstance(){

        Bundle args = new Bundle();

        SmartListRecyclerViewFragment f =  new SmartListRecyclerViewFragment();
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
        Ln.v("On Resume");

    }

    @Override
    public void onPause() {
        super.onPause();
        m_eventBus.unregister(this);
    }

     @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

        m_layoutManager = new LinearLayoutManager(getActivity());

        m_adapter = new SmartListRecyclerViewAdapter(getActivity(),m_smartListDao.monitorSmartListChanges());

        m_masterListView.setLayoutManager(m_layoutManager);
        m_masterListView.setAdapter(m_adapter);

        setRefreshable(false);

    }

}
