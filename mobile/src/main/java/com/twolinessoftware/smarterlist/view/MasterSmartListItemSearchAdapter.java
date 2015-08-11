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
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnListItemSelectedEvent;
import com.twolinessoftware.smarterlist.model.MasterSmartListItem;
import com.twolinessoftware.smarterlist.model.dao.MasterListItemDAO;
import com.twolinessoftware.smarterlist.model.dao.SmartListItemDAO;
import com.twolinessoftware.smarterlist.service.SmartListService;
import com.twolinessoftware.smarterlist.util.Ln;

import javax.inject.Inject;

/**
 * Created by John on 2015-03-26.
 */
public class MasterSmartListItemSearchAdapter extends CursorAdapter {

    private long m_smartListId;
    private final Context m_context;

    @Inject
    Bus m_eventBus;


    @Inject
    MasterListItemDAO m_masterListItemDao;

    @Inject
    SmartListItemDAO m_smartListItemDao;


    @Inject
    SmartListService m_smartListService;

    private final Handler m_refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            notifyDataSetChanged();
        }
    };

    public MasterSmartListItemSearchAdapter(Context context, Cursor cursor, long smartListId) {
        super(context, cursor, true);
        this.m_context = context;
        this.m_smartListId = smartListId;
        Injector.inject(this);
    }

    public void setSmartListId(long smartListId){
        this.m_smartListId = smartListId;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listview, parent, false);
        v.setTag(new GenericListViewHolder(v));

        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        GenericListViewHolder holder = (GenericListViewHolder) view.getTag();


        try {
            final MasterSmartListItem item = m_masterListItemDao.fromCursor(cursor);

            boolean isIncludedInSmartList = m_smartListService.containsItem(item.getId(), m_smartListId);

            holder.getView().setOnClickListener(v1 -> {
                    m_eventBus.post(new OnListItemSelectedEvent(item));
            });

            holder.setText(item.getName());
            holder.textMain.setTextColor(item.getCategoryColor());
            holder.colorSideBar.setBackgroundColor(item.getCategoryColor());

            if (isIncludedInSmartList) {
                holder.icon.setImageResource(R.drawable.ic_check);
            } else {
                TextDrawable textDrawable = new TextDrawable(m_context, item.getName().substring(0, 1));
                holder.icon.setImageDrawable(textDrawable);
            }

            holder.icon.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Ln.e("Unable to parse cursor:" + Log.getStackTraceString(e));
        }

    }
}




