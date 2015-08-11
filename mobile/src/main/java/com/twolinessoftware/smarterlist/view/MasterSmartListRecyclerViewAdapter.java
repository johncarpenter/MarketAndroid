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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnSmartListSelectedEvent;
import com.twolinessoftware.smarterlist.model.MasterSmartList;
import com.twolinessoftware.smarterlist.util.Ln;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by John on 2015-03-26.
 */
public class MasterSmartListRecyclerViewAdapter extends RecyclerView.Adapter<GenericListViewHolder>{

    private final Context m_context;

    private final Observable<List<MasterSmartList>> m_queryObservable;

    public List<MasterSmartList> m_entries = new ArrayList<>();

    @Inject
    Bus m_eventBus;

    private final Handler m_refreshHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            notifyDataSetChanged();
        }
    };
    private String m_selectedName;

    public MasterSmartListRecyclerViewAdapter(Context context, Observable<List<MasterSmartList>> queryObservable) {
        this.m_context = context;
        this.m_queryObservable = queryObservable;
        Injector.inject(this);
    }

    private final Subscriber<List<MasterSmartList>> m_masterListChangesSubscriber = new Subscriber<List<MasterSmartList>>(){

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Ln.e("Error loading data: "+ Log.getStackTraceString(e));
        }

        @Override
        public void onNext(List<MasterSmartList> items) {

                m_entries = items;
                m_refreshHandler.sendEmptyMessage(0);


        }
    };

    private void registerObservers() {

        m_queryObservable
                .subscribeOn(Schedulers.newThread())
                .subscribe(m_masterListChangesSubscriber);

    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        registerObservers();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        m_masterListChangesSubscriber.unsubscribe();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public GenericListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listview, parent, false);


        GenericListViewHolder vh = new GenericListViewHolder(v);
        v.setOnClickListener(v1 -> {
            MasterSmartList item = (MasterSmartList) v1.getTag();

            ImageView imageView = (ImageView) v1.findViewById(R.id.imagecheck_icon);
            if (item.getName().equals(m_selectedName)){
                Picasso.with(m_context).load(item.getIconUrl()).into(imageView);
                m_selectedName = null;
                m_eventBus.post(new OnSmartListSelectedEvent(null));
            } else {
                m_selectedName = item.getName();

                m_eventBus.post(new OnSmartListSelectedEvent(item));
            }



        });

        return vh;
    }

    @Override
    public void onBindViewHolder(GenericListViewHolder holder, int position) {

        MasterSmartList item = m_entries.get(position);

        holder.getView().setTag(item);
        holder.setText(item.getDisplayName());

        if(item.getName().equals(m_selectedName)){
            holder.icon.setImageResource(R.drawable.ic_check);
        } else {
            Picasso.with(m_context).load(item.getIconUrl()).error(R.drawable.ic_empty).into(holder.icon);
        }

        holder.setCaption(item.getDescription());

    }

    @Override
    public int getItemCount() {
        return m_entries.size();
    }


}




