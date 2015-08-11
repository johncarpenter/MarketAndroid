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

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.activity.PlanViewPagerActivity;
import com.twolinessoftware.smarterlist.model.SmartList;
import com.twolinessoftware.smarterlist.model.dao.SmartListItemDAO;
import com.twolinessoftware.smarterlist.view.SmartListItemRecyclerViewAdapter;

import javax.inject.Inject;


public class SmartItemListViewRecyclerViewFragment extends BaseRecyclerViewFragment {

    private static final String EXTRA_SMART_LIST = "EXTRA_SMART_LIST";
    private static final String EXTRA_SELECTION_MODE = "EXTRA_SELECTION_MODE";

    @Inject
    protected Bus m_eventBus;

    @Inject
    SmartListItemDAO m_smartListItemDao;

    private SmartList m_smartList;
    private SmartListItemRecyclerViewAdapter.SelectionMode m_selectionMode;

    public static SmartItemListViewRecyclerViewFragment newInstance(SmartList smartList,SmartListItemRecyclerViewAdapter.SelectionMode selectionMode){

        Bundle args = new Bundle();
        args.putParcelable(EXTRA_SMART_LIST,smartList);
        args.putString(EXTRA_SELECTION_MODE,selectionMode.toString());

        SmartItemListViewRecyclerViewFragment f =  new SmartItemListViewRecyclerViewFragment();
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);

        m_smartList = getArguments().getParcelable(EXTRA_SMART_LIST);
        m_selectionMode = SmartListItemRecyclerViewAdapter.SelectionMode.valueOf(getArguments().getString(EXTRA_SELECTION_MODE));


    }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.shop_pager_title);
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
    public void onStop() {
        super.onStop();

       // List<SmartListItem> items = ((MultiSelectGenericListAdapter)m_adapter).getSelectedItems();
       // m_smartListItemDao.asyncSetChecked(items);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        m_layoutManager = new GridLayoutManager(getActivity(),getSpanCount());

        m_adapter = new SmartListItemRecyclerViewAdapter(getActivity(), m_smartListItemDao.monitorSmartListUnchecked(m_smartList.getItemId()),m_selectionMode);

        m_masterListView.setLayoutManager(m_layoutManager);
        m_masterListView.setAdapter(m_adapter);
        setEmptyViewEnable(true);
        getEmptyView().setOnClickListener(v1->{

            Intent intent = new Intent(getActivity(),PlanViewPagerActivity.class);
            intent.putExtra(PlanViewPagerActivity.EXTRA_SMART_LIST,m_smartList);

            TaskStackBuilder.create(getActivity())
                    .addParentStack(PlanViewPagerActivity.class)
                    .addNextIntent(intent)
                    .startActivities();

            getActivity().finish();
        });

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.shopping_menu,menu);
    }

}
