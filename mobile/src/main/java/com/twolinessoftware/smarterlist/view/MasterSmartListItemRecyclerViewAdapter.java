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
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnListItemSelectedEvent;
import com.twolinessoftware.smarterlist.event.OnNotesIconSelectedEvent;
import com.twolinessoftware.smarterlist.model.MasterSmartListItem;
import com.twolinessoftware.smarterlist.model.SmartListItem;
import com.twolinessoftware.smarterlist.model.dao.SmartListItemDAO;
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
public class MasterSmartListItemRecyclerViewAdapter extends MultiSelectGenericListAdapter<MasterSmartListItem>{

    private final long m_smartListId;

    @Inject
    Bus m_eventBus;

    public List<SmartListItem> m_smartListCache = new ArrayList<SmartListItem>();

    @Inject
    SmartListItemDAO m_smartListItemDao;

    @Inject
    InputMethodManager m_inputMethodManager;

    private final Handler m_refreshHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            notifyDataSetChanged();
        }
    };

    private final Handler m_showImeRunnable = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            View view = (View) msg.obj;
            if (m_inputMethodManager != null) {
                m_inputMethodManager.showSoftInput(view, 0);
            }
        }
    };

    public MasterSmartListItemRecyclerViewAdapter(Context context, Observable<List<MasterSmartListItem>> queryObservable, long smartListId) {
        super(context,queryObservable);
        this.m_smartListId = smartListId;
        Injector.inject(this);
    }

    private SmartListItemChangesObserver m_smartListChangesSubscriber;

    private class SmartListItemChangesObserver extends Subscriber<List<SmartListItem>>{

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(List<SmartListItem> smartListItems) {
            synchronized (m_smartListCache){
                m_smartListCache = smartListItems;

            }
            m_refreshHandler.sendEmptyMessage(0);
        }
    }


    @Override
    public void onRegisterObservers() {
        if (validateAgainstSmartList()){

            if(m_smartListChangesSubscriber != null){
                m_smartListChangesSubscriber.unsubscribe();
            }
            m_smartListChangesSubscriber = new SmartListItemChangesObserver();

            m_smartListItemDao.monitorSmartListUnchecked(m_smartListId)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(m_smartListChangesSubscriber);
        }
    }

    @Override
    public void onUnsubscribeObservers() {
        m_smartListChangesSubscriber.unsubscribe();
    }

    public void swapQueries(Observable<List<MasterSmartListItem>> newQuery){
        setQuery(newQuery);
    }

    private SmartListItem getSmartListItem(long itemId){

        if(validateAgainstSmartList()){

            for(SmartListItem item:m_smartListCache){
                if(item.getMasterItemId()==itemId)
                    return item;
            }

        }

        return null;
    }


    private boolean isIncludedInSmartList(long itemId){

        return getSmartListItem(itemId) != null;
    }

    private boolean validateAgainstSmartList(){
        return m_smartListId != 0;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(MasterSmartListItem item,GenericListViewHolder holder, int position) {

        if(isIncludedInSmartList(item.getId())){
            setSelected(position,true);
        }

        holder.itemView.setOnClickListener(v -> {
            m_eventBus.post(new OnListItemSelectedEvent(item));
        });

        holder.setText(item.getName());
        holder.textMain.setTextColor(item.getCategoryColor());
        holder.colorSideBar.setBackgroundColor(item.getCategoryColor());

        if(isSelected(position)){

            final SmartListItem smartListItem = getSmartListItem(item.getId());

            if(smartListItem != null) {

                holder.setCaption(smartListItem.getNotes());

                holder.icon.setImageResource(R.drawable.ic_check);
                holder.setIconRight(R.drawable.ic_action_new_label);

                holder.iconRight.setOnClickListener(v -> m_eventBus.post(new OnNotesIconSelectedEvent(smartListItem)));
            }else{
                Ln.e("SmartlistItem is null id:"+item.getId());
            }
        }else{
            TextDrawable textDrawable = new TextDrawable(m_context,item.getName().substring(0,1));
            holder.icon.setImageDrawable(textDrawable);
            holder.removeIconRight();
            holder.setCaption(null);
        }
        holder.icon.setVisibility(View.VISIBLE);
    }

}




