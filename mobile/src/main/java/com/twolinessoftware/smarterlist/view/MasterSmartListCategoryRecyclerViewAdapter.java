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
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnNavigateToCategory;
import com.twolinessoftware.smarterlist.model.MasterSmartCategory;
import com.twolinessoftware.smarterlist.model.MasterSmartListItem;
import com.twolinessoftware.smarterlist.model.SmartListItem;
import com.twolinessoftware.smarterlist.model.dao.SmartListItemDAO;
import com.twolinessoftware.smarterlist.util.Ln;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by John on 2015-03-26.
 */
public class MasterSmartListCategoryRecyclerViewAdapter extends RecyclerView.Adapter<GenericListViewHolder> implements AdapterLifecycleInterface{

    private final Context m_context;

    private final Observable<List<MasterSmartListItem>> m_queryObservable;

    private final long m_smartListId;


    public List<SmartListItem> m_smartListCache = new ArrayList<SmartListItem>();

    public List<MasterSmartCategory> m_entries = new ArrayList<MasterSmartCategory>();

    @Inject
    SmartListItemDAO m_smartListItemDao;

    @Inject
    Bus m_eventBus;

    private final Handler m_refreshHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            notifyDataSetChanged();
        }
    };

    public MasterSmartListCategoryRecyclerViewAdapter(Context context, Observable<List<MasterSmartListItem>> queryObservable, long smartListId) {
        this.m_context = context;
        this.m_queryObservable = queryObservable;
        this.m_smartListId = smartListId;

        Injector.inject(this);


    }

    private MasterSmartItemChangesSubscriber m_masterListChangesSubscriber;
    private class MasterSmartItemChangesSubscriber extends Subscriber<List<MasterSmartListItem>>{


        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Ln.e("Error loading data: "+ Log.getStackTraceString(e));
        }

        @Override
        public void onNext(List<MasterSmartListItem> smartListItemList) {

            List<MasterSmartCategory> items = mapCategories(smartListItemList);

            Ln.v("Updating list with " + items.size() + " items");

            synchronized (m_entries) {

                ArrayList<MasterSmartCategory> removed = new ArrayList<>(m_entries);
                removed.removeAll(items);

                Ln.v("Removing " + removed.size() + " items");

                // delete the removed items from the current list
                m_entries.removeAll(removed);

                // determine the new items by removing the existing items from the list
                items.removeAll(m_entries);

                Ln.v("Adding " + items.size() + " items");

                int position = m_entries.size();
                for (MasterSmartCategory item : items) {
                    m_entries.add(item);
                }
                notifyItemRangeInserted(position, items.size());

                notifyDataSetChanged();
            }

        }
    }

    private MasterListItemSubscriber m_smartListChangesSubscriber;
    private class MasterListItemSubscriber extends Subscriber<List<SmartListItem>>{

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(List<SmartListItem> smartListItems) {
            Ln.v("Updating cache:"+smartListItems.size());
            synchronized (m_smartListCache){
                m_smartListCache = smartListItems;
            }
            m_refreshHandler.sendEmptyMessage(0);
        }
    }


    private void registerObservers() {

        if(m_masterListChangesSubscriber != null){
            m_masterListChangesSubscriber.unsubscribe();
        }
        m_masterListChangesSubscriber = new MasterSmartItemChangesSubscriber();
        m_queryObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(m_masterListChangesSubscriber);

        if (validateAgainstSmartList()){

            if(m_smartListChangesSubscriber != null){
                m_smartListChangesSubscriber.unsubscribe();
            }
            m_smartListChangesSubscriber = new MasterListItemSubscriber();

            m_smartListItemDao.monitorSmartList(m_smartListId)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(m_smartListChangesSubscriber);


        }


    }

    private boolean validateAgainstSmartList(){
        return m_smartListId != 0;
    }

    private String getCaptionText(long categoryId){
        if(validateAgainstSmartList()){
            StringBuilder sb = new StringBuilder();
            for(SmartListItem item:m_smartListCache){
                if(item.getCategoryId() == categoryId && !item.isChecked()){
                    if((sb.length() + item.getName().length()) < 80){
                        if (sb.length() > 0){
                            sb.append(", ");
                        }
                        sb.append(item.getName());
                    }else{
                        sb.append("...");
                        break;
                    }
                }
            }

            return sb.toString();
        }
        return null;
    }

    @Override
    public void onPause() {
        m_masterListChangesSubscriber.unsubscribe();
        m_smartListChangesSubscriber.unsubscribe();
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
    public GenericListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listview, parent, false);

        GenericListViewHolder vh = new GenericListViewHolder(v);

        v.setOnClickListener(v1 -> {
            MasterSmartCategory category = (MasterSmartCategory) v1.getTag();
            ViewCompat.setTransitionName(v1,"viewtransition_"+category.getId());

            m_eventBus.post(new OnNavigateToCategory(v1,category));
        });


        return vh;
    }

    @Override
    public void onBindViewHolder(GenericListViewHolder holder, int position) {

        MasterSmartCategory item = m_entries.get(position);

        holder.getView().setTag(item);
        holder.setText(item.getName());

        String caption = getCaptionText(item.getId());
        holder.setCaption(caption);
        holder.textMain.setTextColor(item.getColor());
        holder.colorSideBar.setBackgroundColor(item.getColor());

        // @todo add error image
       Picasso.with(m_context).load(item.getIconUrl()).into(holder.icon);


    }

    @Override
    public int getItemCount() {
        return m_entries.size();
    }

    private List<MasterSmartCategory> mapCategories(List<MasterSmartListItem> items) {
        HashSet<MasterSmartCategory> categories = new HashSet<>();

        for(MasterSmartListItem item:items){
            MasterSmartCategory category = new MasterSmartCategory(item.getCategoryId(),item.getCategoryName(),item.getCategoryColor(),item.getCategoryIconUrl());
            categories.add(category);
         }

         return new ArrayList<>(categories);

    }

}




