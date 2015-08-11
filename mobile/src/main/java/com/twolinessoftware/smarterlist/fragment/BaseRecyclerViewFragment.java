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

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.util.UIUtils;
import com.twolinessoftware.smarterlist.view.AdapterLifecycleInterface;

import java.lang.ref.WeakReference;

import butterknife.InjectView;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

/**
 * Created by John on 2015-04-01.
 */
public abstract class BaseRecyclerViewFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.refresh_layout)
    protected SwipeRefreshLayout m_swipeRefreshLayout;

    @InjectView(R.id.list_master)
    RecyclerView m_masterListView;

    @InjectView(R.id.layout_empty)
    ViewGroup m_emptyLayoutView;

    private boolean m_onViewCreatedCalled;

    protected RecyclerView.Adapter m_adapter;

    protected RecyclerView.LayoutManager m_layoutManager;

    @SuppressLint("HandlerLeak")
    public final Handler m_delayShowEmptyHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            setEmptyViewVisibile((m_adapter.getItemCount() == 0));
        }
    };
    private boolean m_emptyViewEnabled;

    @Override
    protected int setContentView() {
        return R.layout.fragment_masterlistview;
    }

    protected WeakReference<RecyclerView.AdapterDataObserver> m_dataObserver = new WeakReference<>(new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            setRefreshing(false);

            // Item loading may be delayed, lets not show the empty right away
            m_delayShowEmptyHandler.sendEmptyMessageDelayed(0, 250);
        }
    });


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


        m_swipeRefreshLayout.setOnRefreshListener(this);
        m_onViewCreatedCalled = true;
        setRefreshable(false);

        super.onViewCreated(view, savedInstanceState);

        m_masterListView.setItemAnimator(new SlideInLeftAnimator());
        m_masterListView.getItemAnimator().setRemoveDuration(250);

      //  if (m_adapter != null) {
       //     m_adapter.registerAdapterDataObserver(m_dataObserver.get());
      //  }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (!m_onViewCreatedCalled) {
            throw new RuntimeException("RecyclerFragment requires super.onViewCreated");
        }

        if (m_adapter != null) {
            ((AdapterLifecycleInterface) m_adapter).onResume();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if (m_adapter != null) {
            ((AdapterLifecycleInterface) m_adapter).onPause();
        }
        m_delayShowEmptyHandler.removeMessages(0);


    }

    public void setEmptyViewEnable(boolean enabled) {
        if (!m_onViewCreatedCalled) {
            throw new RuntimeException("RecyclerFragment requires super.onViewCreated");
        }
        m_emptyViewEnabled = enabled;
    }

    public void setRefreshable(boolean refreshable) {

        if (!m_onViewCreatedCalled) {
            throw new RuntimeException("RecyclerFragment requires super.onViewCreated");
        }

        m_swipeRefreshLayout.setEnabled(refreshable);
    }

    public void setRefreshing(boolean refreshing) {

        if (!m_onViewCreatedCalled) {
            throw new RuntimeException("RecyclerFragment requires super.onViewCreated");
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            if (m_swipeRefreshLayout != null) {
                m_swipeRefreshLayout.setRefreshing(refreshing);
            }
        });


    }

    public void setEmptyViewVisibile(boolean visible) {
        if (m_emptyViewEnabled) {
            m_masterListView.setVisibility(visible ? View.GONE : View.VISIBLE);
            m_emptyLayoutView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public ViewGroup getEmptyView() {
        return m_emptyLayoutView;
    }

    @Override
    public void onRefresh() {

    }

    protected int getSpanCount() {
        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE || UIUtils.isTablet(getActivity()))
            return 2;
        else
            return 1;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (m_layoutManager != null && m_layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) m_layoutManager).setSpanCount(getSpanCount());
        }

    }


}
