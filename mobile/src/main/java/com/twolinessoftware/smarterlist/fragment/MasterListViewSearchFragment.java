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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.activity.PlanViewPagerActivity;
import com.twolinessoftware.smarterlist.activity.SearchActivity;
import com.twolinessoftware.smarterlist.event.OnAddNewMasterListItemEvent;
import com.twolinessoftware.smarterlist.event.OnListItemSelectedEvent;
import com.twolinessoftware.smarterlist.model.dao.MasterListItemDAO;
import com.twolinessoftware.smarterlist.util.Ln;
import com.twolinessoftware.smarterlist.view.MasterSmartListItemSearchAdapter;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;

public class MasterListViewSearchFragment extends BaseFragment {

    private static final String EXTRA_MASTERLIST_NAME = "EXRTA_MASTER_LIST_NAME";

    @Inject
    protected Bus m_eventBus;

    @Inject
    MasterListItemDAO m_masterListItemDAO;

    @InjectView(R.id.search_view)
    SearchView m_searchView;

    @InjectView(R.id.button_search)
    Button m_searchButton;

    private Observable<String> m_queryObserver;

    private String m_lastSearchQuery;

    private MasterSmartListItemSearchAdapter m_searchSuggestionAdapter;

    private long m_smartListId;


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

        if (m_searchView != null) {
            m_searchView.setQuery(null, false);
        }
    }

    public void setSmartListId(long smartListId){
        this.m_smartListId = smartListId;
//        m_searchSuggestionAdapter.setSmartListId(smartListId);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       // attachSearchView();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getBaseActivity(), getView(), SearchActivity.IMAGE_TRANSITION_NAME);
                Intent intent = new Intent(getBaseActivity(), SearchActivity.class);
                intent.putExtra(PlanViewPagerActivity.EXTRA_SMART_LIST,m_smartListId);
                ActivityCompat.startActivity(getBaseActivity(), intent, options.toBundle());
            }
        });


    }

    private void updateQuery(String newSearchTerm) {

        Ln.v("Updating Query:" + newSearchTerm);

        m_lastSearchQuery = newSearchTerm;

        m_searchSuggestionAdapter.changeCursor(buildQueryCursor(newSearchTerm+"*"));
        m_searchSuggestionAdapter.notifyDataSetChanged();

        toggleAddButton(!TextUtils.isEmpty(newSearchTerm) && m_searchSuggestionAdapter.getCount()==0);
    }

    public void attachSearchView() {

        m_searchView.setIconified(false);

        m_searchSuggestionAdapter = new MasterSmartListItemSearchAdapter(getBaseActivity(),null,m_smartListId);
        m_searchView.setSuggestionsAdapter(m_searchSuggestionAdapter);

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
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    updateQuery(s);
                });

    }

    private Cursor buildQueryCursor(String queryText){
       return  getBaseActivity().getContentResolver().query(Uri.withAppendedPath(m_masterListItemDAO.getProvider().getBaseContentUri(), "search"), null, "masterListName = ? and name MATCH ?",new String[]{Constants.DEFAULT_MASTERLIST_NAME,queryText},"categoryId desc");

    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_search;
    }

    private void toggleAddButton(boolean visible) {
        m_searchButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Subscribe
    public void onListItemSelected(OnListItemSelectedEvent event) {
       // m_searchView.setQuery("", true);
       // m_searchView.clearFocus();
    }

    @OnClick(R.id.button_search)
    public void onAddClicked(View view) {
        m_eventBus.post(new OnAddNewMasterListItemEvent(m_lastSearchQuery,Constants.DEFAULT_MASTERLIST_NAME));

    }

}
