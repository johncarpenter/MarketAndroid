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

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;

import com.squareup.picasso.Picasso;
import com.twolinessoftware.smarterlist.fragment.SmartItemListViewRecyclerViewFragment;
import com.twolinessoftware.smarterlist.model.SmartList;
import com.twolinessoftware.smarterlist.util.Ln;
import com.twolinessoftware.smarterlist.view.SmartListItemRecyclerViewAdapter;

import java.io.IOException;


public class ShoppingNavigationActivity extends BaseNavigationActivity {

    public static final String EXTRA_SMART_LIST = "EXTRA_SMART_LIST";
    public static final String IMAGE_TRANSITION_NAME = "image_transition_name";

    private SmartList m_smartList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateView(getIntent());

        ViewCompat.setTransitionName(getToolbar(),IMAGE_TRANSITION_NAME);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateView(intent);
    }

    private void updateView(Intent intent) {

        if(intent == null || !intent.hasExtra(EXTRA_SMART_LIST)) {
            Ln.e("Missing SmartList Id");
            finish();
        }

        m_smartList = intent.getParcelableExtra(EXTRA_SMART_LIST);

        setTitle(m_smartList.getName());

        showFragment(SmartItemListViewRecyclerViewFragment.newInstance(m_smartList, SmartListItemRecyclerViewAdapter.SelectionMode.Check));
    }

    private void loadLogoIntoToolbar() {
        BitmapDrawable drawable = null;
        try {
            drawable = new BitmapDrawable(getResources(), Picasso.with(this).load(m_smartList.getIconUrl()).get());
            getToolbar().setLogo(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
