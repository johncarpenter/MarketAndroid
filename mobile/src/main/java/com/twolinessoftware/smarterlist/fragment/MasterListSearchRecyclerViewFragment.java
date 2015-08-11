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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.activity.SearchActivity;
import com.twolinessoftware.smarterlist.event.OnAddNewMasterListItemEvent;
import com.twolinessoftware.smarterlist.event.OnListItemSelectedEvent;
import com.twolinessoftware.smarterlist.model.dao.MasterListItemDAO;
import com.twolinessoftware.smarterlist.util.Ln;
import com.twolinessoftware.smarterlist.view.MasterSmartListItemRecyclerViewAdapter;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MasterListSearchRecyclerViewFragment extends BaseRecyclerViewFragment {

    private static final String EXTRA_KEY_CATEGORY_ID = "EXTRA_KEY_CATEGORY_ID";
    private static final String EXTRA_SMART_LIST_ID = "EXTRA_SMART_LIST_ID";
    private static final String EXTRA_MASTERLIST_NAME = "EXRTA_MASTER_LIST_NAME";

    @Inject
    protected Bus m_eventBus;

    @Inject
    MasterListItemDAO m_masterListItemDAO;

    @InjectView(R.id.search_view)
    SearchView m_searchView;

    @InjectView(R.id.button_search)
    Button m_searchButton;

    private long m_categoryId;

    private long m_smartListId;

    private String m_masterListName;
    private String m_lastSearchQuery;

    private Observable<String> m_queryObserver;

    private final Handler m_toggleAddHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            toggleAddButton(!TextUtils.isEmpty(m_lastSearchQuery)&&m_adapter.getItemCount()==0);
        }
    };

    public static MasterListSearchRecyclerViewFragment newInstance(String masterListName, long categoryId, long smartListId) {

        Bundle args = new Bundle();

        args.putString(EXTRA_MASTERLIST_NAME, masterListName);
        args.putLong(EXTRA_KEY_CATEGORY_ID, categoryId);
        args.putLong(EXTRA_SMART_LIST_ID, smartListId);

        MasterListSearchRecyclerViewFragment f = new MasterListSearchRecyclerViewFragment();
        f.setArguments(args);

        return f;
    }


  @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injector.inject(this);


        m_categoryId = getArguments().getLong(EXTRA_KEY_CATEGORY_ID, 0);
        m_smartListId = getArguments().getLong(EXTRA_SMART_LIST_ID, 0);
        m_masterListName = getArguments().getString(EXTRA_MASTERLIST_NAME);

    }

    @Override
    public void onResume() {
        super.onResume();
        m_eventBus.register(this);
        clearSearchQuery();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_eventBus.unregister(this);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setTransitionName(m_searchView, SearchActivity.IMAGE_TRANSITION_NAME);

        m_layoutManager = new GridLayoutManager(getActivity(), getSpanCount());

        m_adapter = new MasterSmartListItemRecyclerViewAdapter(getActivity(), m_masterListItemDAO.search(m_masterListName, m_categoryId, "*"), m_smartListId);
        ((MasterSmartListItemRecyclerViewAdapter)m_adapter).setOnDataLoadedListener(() -> {
            m_toggleAddHandler.sendEmptyMessage(0);
        });

        m_masterListView.setLayoutManager(m_layoutManager);
        m_masterListView.setAdapter(m_adapter);

        attachSearchView();
    }

    public void clearSearchQuery(){
        m_searchView.setQuery("", true);
        m_searchView.requestFocus();
        toggleAddButton(false);
    }

    private void updateQuery(String newSearchTerm) {

        Ln.v("Updating Query:" + newSearchTerm);

        m_lastSearchQuery = newSearchTerm;

        ((MasterSmartListItemRecyclerViewAdapter) m_adapter).swapQueries(m_masterListItemDAO.search(m_masterListName, m_categoryId, newSearchTerm + "*"));

    }



    public void attachSearchView() {

       this.m_queryObserver = Observable.create(subscriber -> {
            m_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    subscriber.onNext(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    subscriber.onNext(s);
                    return true;
                }
            });
        });

        AndroidObservable.bindFragment(this, m_queryObserver)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    updateQuery(s);
                });

    }


    @Override
    protected int setContentView() {
        return R.layout.fragment_masterlistview_search;
    }

    private void toggleAddButton(boolean visible) {
        m_searchButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Subscribe
    public void onListItemSelected(OnListItemSelectedEvent event) {
        clearSearchQuery();
    }

    @OnClick(R.id.button_search)
    public void onAddClicked(View view) {
        m_eventBus.post(new OnAddNewMasterListItemEvent(m_lastSearchQuery,Constants.DEFAULT_MASTERLIST_NAME));

    }



}
