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
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.fragment.HeroFragment;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class TutorialActivity extends FragmentActivity {

    @InjectView(R.id.button_next)
    Button m_buttonNext;


    @InjectView(R.id.view_pager)
    ViewPager m_pager;

    @InjectView(R.id.viewpager_indicator)
    CirclePageIndicator m_pagerIndicator;

    private TutorialPager m_adapter;

    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    private int m_currentApiVersion;


    private final ViewPager.OnPageChangeListener endPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            m_buttonNext.setText(isLastPage()?getString(R.string.tutorial_signup):getString(R.string.tutorial_nextdone));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        m_currentApiVersion = android.os.Build.VERSION.SDK_INT;

        // This work only for android 4.4+
        if(m_currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                    {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility)
                        {
                            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                            {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }

        setContentView(R.layout.activity_tutorial);

        ButterKnife.inject(this);

        configureFragments();
    }


    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(m_currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    private void configureFragments() {

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(HeroFragment.newInstance(R.color.pal_4, R.drawable.hero_1, 0, R.string.hero_caption1));
        fragments.add(HeroFragment.newInstance(R.color.pal_4, R.drawable.hero_2, R.string.hero_title2, R.string.hero_caption2));
        fragments.add(HeroFragment.newInstance(R.color.pal_4, R.drawable.hero_3, R.string.hero_title3, R.string.hero_caption3));
        fragments.add(HeroFragment.newInstance(R.color.pal_4, R.drawable.hero_4, R.string.hero_title4, R.string.hero_caption4));

        m_adapter = new TutorialPager(getSupportFragmentManager(),fragments);
        m_pager.setAdapter(m_adapter);

        m_pagerIndicator.setViewPager(m_pager);
        m_pagerIndicator.setOnPageChangeListener(endPageChangeListener);
    }

    @OnClick(R.id.button_next)
    public void onNextClick(View view){
        if(isLastPage()){
            setResult(Activity.RESULT_OK);
            finish();
        }else {
            nextPage();
        }
    }

    @OnClick(R.id.button_skip)
    public void onSkipClick(View view){
        setResult(Activity.RESULT_OK);
        finish();
    }

    private boolean isLastPage(){
        return m_pager.getCurrentItem() == (m_adapter.getCount()-1);
    }



    private void nextPage() {
        int currentPage = m_pager.getCurrentItem();
        if(!isLastPage()){
            m_pager.setCurrentItem(currentPage+1);
        }
    }




    private class TutorialPager extends FragmentStatePagerAdapter {

        private final List<Fragment> fragments;

        public TutorialPager(FragmentManager fm, List<Fragment> maneuverFragments) {
            super(fm);
            this.fragments = maneuverFragments;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

}
