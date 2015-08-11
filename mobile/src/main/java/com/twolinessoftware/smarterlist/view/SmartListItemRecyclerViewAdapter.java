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

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnNotesIconSelectedEvent;
import com.twolinessoftware.smarterlist.model.SmartListItem;
import com.twolinessoftware.smarterlist.model.dao.SmartListItemDAO;
import com.twolinessoftware.smarterlist.util.AccountUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public class SmartListItemRecyclerViewAdapter extends MultiSelectGenericListAdapter<SmartListItem>{

    private final SelectionMode m_selectionMode;

    public enum SelectionMode{Delete,Check}

    @Inject
    SmartListItemDAO m_smartListItemDao;

    @Inject
    Bus m_eventBus;


    @Inject
    AccountUtils m_accountUtils;

    public SmartListItemRecyclerViewAdapter(Context context, Observable<List<SmartListItem>> queryObservable, SelectionMode selectionMode) {
        super(context,queryObservable);
        this.m_selectionMode = selectionMode;
        Injector.inject(this);
    }


    @Override
    public void onBindViewHolder(final SmartListItem item, GenericListViewHolder holder, int position) {

        holder.itemView.setOnClickListener(v1 -> {
            if (m_selectionMode == SelectionMode.Check) {
                m_smartListItemDao.toggleChecked(item);
            } else {
                m_smartListItemDao.toggleIncluded(item);
            }

            m_accountUtils.scheduleSmartlistSync();

        });

        holder.icon.setImageResource(R.drawable.ic_check);
        holder.setText(item.getName());
        holder.textMain.setTextColor(item.getCategoryColor());
        holder.colorSideBar.setBackgroundColor(item.getCategoryColor());

        holder.setCaption(item.getNotes());

        // Don't include notes on the shopping page

        if (m_selectionMode != SelectionMode.Check) {
            holder.setIconRight(R.drawable.ic_action_new_label);
            holder.iconRight.setOnClickListener(v -> m_eventBus.post(new OnNotesIconSelectedEvent(item)));
        }else{
            holder.setIconRight(0);
        }

    }

}




