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

package com.twolinessoftware.smarterlist.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnEditListSelectEvent;
import com.twolinessoftware.smarterlist.event.OnOverflowSelectedEvent;
import com.twolinessoftware.smarterlist.event.OnShoppingListSelectEvent;
import com.twolinessoftware.smarterlist.model.SmartList;
import com.twolinessoftware.smarterlist.model.dao.SmartListItemDAO;
import com.twolinessoftware.smarterlist.util.AccountUtils;
import com.twolinessoftware.smarterlist.util.Ln;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by John on 2015-03-26.
 */
public class SmartListCardRecyclerViewAdapter extends RecyclerView.Adapter<GenericCardViewHolder> implements AdapterLifecycleInterface{

    private final Context m_context;
    private Observable<List<SmartList>> m_queryObservable;

    public List<SmartList> m_entries = new ArrayList<SmartList>();

    private HashMap<Long,Integer> m_uncheckedCounts = new HashMap<>();


    @Inject
    SmartListItemDAO m_smartListItemDao;

    @Inject
    Bus m_eventBus;

    @Inject
    AccountUtils m_accountUtils;


    private final Handler m_refreshHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            notifyDataSetChanged();
        }
    };


    public SmartListCardRecyclerViewAdapter(Context context, Observable<List<SmartList>> queryObservable) {
        this.m_context = context;
        this.m_queryObservable = queryObservable;
        Injector.inject(this);

    }

    private SmartListChangesSubscriber m_masterListChangesSubscriber;
    private class SmartListChangesSubscriber extends Subscriber<List<SmartList>>{


        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Ln.e("Error loading data: "+ Log.getStackTraceString(e));
        }

        @Override
        public void onNext(List<SmartList> items) {
                Ln.v("SmartList updates. Showing "+items.size()+" cards");
                m_entries = items;
                m_refreshHandler.sendEmptyMessage(0);

        }
    }

    private UncheckedCountSubscriber m_uncheckedCountSubscriber;
    private class UncheckedCountSubscriber extends Subscriber<HashMap<Long, Integer>> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Ln.e("Error processing counts:"+Log.getStackTraceString(e));
        }

        @Override
        public void onNext(HashMap<Long, Integer> mapUpdatingCounts) {
            Ln.v("Unchecked count updates");
            m_uncheckedCounts = mapUpdatingCounts;
            m_refreshHandler.sendEmptyMessage(0);

        }
    }


    private void registerObservers() {
        Ln.v("Registering card observers");
        m_masterListChangesSubscriber = new SmartListChangesSubscriber();
        m_queryObservable
                .subscribeOn(Schedulers.newThread())
                .subscribe(m_masterListChangesSubscriber);

        m_uncheckedCountSubscriber = new UncheckedCountSubscriber();
        m_smartListItemDao.monitorSmartListUncheckedCounts()
                .subscribeOn(Schedulers.newThread())
                .subscribe(m_uncheckedCountSubscriber);
    }




    @Override
    public void onPause() {
        Ln.v("Pausing card observers");
        m_masterListChangesSubscriber.unsubscribe();
        m_uncheckedCountSubscriber.unsubscribe();
    }

    @Override
    public void onResume() {
            registerObservers();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public GenericCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cardview, parent, false);


        GenericCardViewHolder vh = new GenericCardViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(GenericCardViewHolder holder, int position) {

        SmartList item = m_entries.get(position);


        holder.getView().setOnClickListener(view ->{
            m_eventBus.post(new OnShoppingListSelectEvent(holder.getView(),item));
        });


        holder.m_createdDate.setText(m_context.getString(R.string.card_date_created, DateUtils.getRelativeTimeSpanString(item.getCreated().getTime(),System.currentTimeMillis(),DateUtils.MINUTE_IN_MILLIS,0)));

        holder.getView().setTag(item);
        holder.setText(item.getName());

        if(m_accountUtils.isOwner(item)){
           holder.getView().setBackgroundColor(m_context.getResources().getColor(R.color.pal_grey_1));
        }else{
            holder.getView().setBackgroundColor(m_context.getResources().getColor(R.color.pal_grey_2));
        }

        // Get Count of Items in SmartList
        Integer countOfItems = m_uncheckedCounts.get(Long.valueOf(item.getItemId()));

        int count = countOfItems != null?countOfItems.intValue():0;

        holder.setCaption(m_context.getResources().getQuantityString(R.plurals.card_caption_items,count,count));


        Picasso.with(m_context).load(item.getIconUrl()).into(holder.icon);

        holder.imageOverflow.setOnClickListener(v1 ->{
            m_eventBus.post(new OnOverflowSelectedEvent(v1,item));
        });

        holder.buttonAction1.setOnClickListener(v1 -> {
            m_eventBus.post(new OnShoppingListSelectEvent(holder.getView(),item));
        });

        holder.buttonAction2.setOnClickListener(v1 -> {
            m_eventBus.post(new OnEditListSelectEvent(holder.getView(),item));
        });


    }

    @Override
    public int getItemCount() {
        return m_entries.size();
    }


}




