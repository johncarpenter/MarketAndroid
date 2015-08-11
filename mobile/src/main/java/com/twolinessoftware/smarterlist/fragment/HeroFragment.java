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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.twolinessoftware.smarterlist.R;

import butterknife.InjectView;

/**
 * Created by John on 2015-04-08.
 */
public class HeroFragment extends BaseFragment {

    private static final String EXTRA_BACKGROUND_ID = "EXTRA_BACKGROUND_ID";
    private static final String EXTRA_HERO_ICON = "EXTRA_HERO_ICON";
    private static final String EXTRA_HERO_TITLE = "EXTRA_HERO_TITLE";
    private static final String EXTRA_HERO_CAPTION = "EXTRA_HERO_CAPTION";

    @InjectView(R.id.layout_hero_container)
    ViewGroup m_layoutContainer;

    @InjectView(R.id.image_hero_icon)
    ImageView m_heroIcon;

    @InjectView(R.id.text_hero_title)
    TextView m_heroTitle;

    @InjectView(R.id.text_hero_caption)
    TextView m_heroCaption;
    private int m_backgroundColor;
    private int m_heroIconResId;
    private int m_heroTitleResId;
    private int m_heroCaptionResId;

    public static HeroFragment newInstance(int backgroundId, int heroIcon, int heroTitle, int heroCaption){

        Bundle args = new Bundle();

        args.putInt(EXTRA_BACKGROUND_ID, backgroundId);
        args.putInt(EXTRA_HERO_ICON,heroIcon);
        args.putInt(EXTRA_HERO_TITLE,heroTitle);
        args.putInt(EXTRA_HERO_CAPTION,heroCaption);

        HeroFragment f =  new HeroFragment();
        f.setArguments(args);

        return f;
    }


    @Override
    protected int setContentView() {
        return R.layout.fragment_hero;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        m_backgroundColor = args.getInt(EXTRA_BACKGROUND_ID);
        m_heroIconResId = args.getInt(EXTRA_HERO_ICON);
        m_heroTitleResId = args.getInt(EXTRA_HERO_TITLE);
        m_heroCaptionResId = args.getInt(EXTRA_HERO_CAPTION);
}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        m_layoutContainer.setBackgroundColor(getResources().getColor(m_backgroundColor));
        m_heroIcon.setImageResource(m_heroIconResId);

        if(m_heroTitleResId == 0){
            m_heroTitle.setVisibility(View.GONE);
        }else {
            m_heroTitle.setText(m_heroTitleResId);
        }

        m_heroCaption.setText(m_heroCaptionResId);

    }
}

