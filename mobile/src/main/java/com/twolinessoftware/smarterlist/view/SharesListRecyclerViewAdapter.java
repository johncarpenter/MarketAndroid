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
import android.graphics.drawable.Drawable;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.model.ShareGroup;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Views the contacts on the local device
 * <p>
 * Created by John on 2015-03-26.
 */
public class SharesListRecyclerViewAdapter extends MultiSelectGenericListAdapter<ShareGroup> {

    @Inject
    Bus m_eventBus;

    public SharesListRecyclerViewAdapter(Context context, Observable<List<ShareGroup>> shareGroupObservable) {
        super(context);
        setQuery(shareGroupObservable);
        Injector.inject(this);
    }


    @Override
    void onBindViewHolder(ShareGroup item, GenericListViewHolder holder, int position) {

        holder.getView().setOnClickListener(v1->{
            if(!item.getRole().equalsIgnoreCase("owner")){
                toggleSelection(position);
            }
        });

        holder.setText(item.getEmail());
        holder.setCaption(item.getRole());

        if(isSelected(position)){
            holder.icon.setImageResource(R.drawable.ic_check);
        }else{
            holder.icon.setImageDrawable(getTextDrawable(item.getEmail()));
        }

    }

    private Drawable getTextDrawable(String name) {
        return new TextDrawable(m_context, name.substring(0, 1));
    }

}




