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
import com.twolinessoftware.smarterlist.event.OnShoppingListSelectEvent;
import com.twolinessoftware.smarterlist.model.SmartList;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public class SmartListRecyclerViewAdapter extends MultiSelectGenericListAdapter<SmartList>{

   @Inject
    Bus m_eventBus;

    public SmartListRecyclerViewAdapter(Context context, Observable<List<SmartList>> queryObservable) {
        super(context,queryObservable);
        Injector.inject(this);
    }

    @Override
    void onBindViewHolder(SmartList item, GenericListViewHolder holder, int position) {

        holder.getView().setOnClickListener(v1->{
            m_eventBus.post(new OnShoppingListSelectEvent(holder.getView(),item));
        });

        holder.setText(item.getName());
        holder.setCaption(item.getDescription());
        holder.icon.setImageResource(R.drawable.ic_empty);
    }


}




