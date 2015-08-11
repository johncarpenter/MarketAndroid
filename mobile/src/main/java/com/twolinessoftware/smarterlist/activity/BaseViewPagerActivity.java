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

package com.twolinessoftware.smarterlist.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnFloatingActionButtonPressedEvent;
import com.twolinessoftware.smarterlist.fragment.BaseFragment;
import com.twolinessoftware.smarterlist.util.Ln;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Base functionality for all ViewPager Activities. Injects butterknife and eventbus.
 */
@SuppressLint("Registered")
public class BaseViewPagerActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    @InjectView(R.id.view_pager)
    ViewPager m_pager;

    @InjectView(R.id.viewpager_indicator)
    TitlePageIndicator m_pagerIndicator;

    @Inject
    protected Bus m_eventBus;

    private StandardFragmentPager m_adapter;

    private ArrayList<BaseFragment> m_fragments;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_fragments = new ArrayList<BaseFragment>();

        m_adapter = new StandardFragmentPager(getSupportFragmentManager(),m_fragments);
        m_pager.setAdapter(m_adapter);

        m_pagerIndicator.setViewPager(m_pager);
        m_pager.addOnPageChangeListener(this);

    }

    protected int getContentView() {
        return R.layout.activity_viewpager;
    }

    public void setFloatingActionButtonVisibility(boolean visible) {
        m_floatingActionButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.button_float)
    public void onFloatingActionButtonPressed(View view) {
        m_eventBus.post(new OnFloatingActionButtonPressedEvent());
    }

    public void addFragment(BaseFragment fragment){
        m_fragments.add(fragment);
    }

    public void refresh(){
        m_adapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Ln.v("Page Selected:" + position);
        m_adapter.getItem(position).onResume();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class StandardFragmentPager extends FragmentPagerAdapter {

        private final List<BaseFragment> fragments;

        public StandardFragmentPager(FragmentManager fm, List<BaseFragment> maneuverFragments) {
            super(fm);
            this.fragments = maneuverFragments;
        }

        @Override
        public BaseFragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getItem(position).getPageTitle(getResources());
        }
    }


}
