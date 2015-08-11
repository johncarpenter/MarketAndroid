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

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twolinessoftware.smarterlist.activity.BaseActivity;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {


    protected abstract int setContentView();

    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(setContentView(),container,false);
        ButterKnife.inject(this,view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    // Finishes the fragment and passes control to the activity
    protected void finish(boolean removeFromStack){
        getBaseActivity().resetActionMode();

        if(removeFromStack) {
            getBaseActivity().onBackPressed();
        }
    }

    public void startActionMode(ActionMode.Callback callback){
        ((BaseActivity)getActivity()).configureActionMode(callback);
    }

    public ActionMode getActionMode(){return ((BaseActivity)getActivity()).getActionMode();}

    public Toolbar getToolbar(){
        return ((BaseActivity)getActivity()).getToolbar();
    }

    public BaseActivity getBaseActivity(){
        return ((BaseActivity)getActivity());
    }

    public String getPageTitle(Resources resources){
        return null;
    }




}
