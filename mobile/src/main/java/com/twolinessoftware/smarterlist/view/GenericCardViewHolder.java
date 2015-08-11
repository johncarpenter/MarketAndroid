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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.twolinessoftware.smarterlist.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by John on 2015-03-29.
 */
public class GenericCardViewHolder extends RecyclerView.ViewHolder{

    private final View m_view;

    @InjectView(R.id.text_created_date)
    public TextView m_createdDate;

    @InjectView(R.id.image_icon)
    public ImageView icon;

   @InjectView(R.id.text_info)
    public TextView textMain;

    @InjectView(R.id.text_info_sub)
    public TextView textCaption;

    @InjectView(R.id.text_action_1)
    public Button buttonAction1;

    @InjectView(R.id.text_action_2)
    public Button buttonAction2;

    @InjectView(R.id.image_overflow)
    public ImageButton imageOverflow;


    public GenericCardViewHolder(View itemView) {
        super(itemView);
        this.m_view = itemView;
        ButterKnife.inject(this,itemView);

    }

    public View getView() {
        return m_view;
    }

    public void setText(String text){
        textMain.setText(text);
    }

    public void setCaption(String caption){
        textCaption.setVisibility(View.VISIBLE);
        textCaption.setText(caption);
    }

    public void removeCaption(){
        textCaption.setVisibility(View.GONE);
    }

}
