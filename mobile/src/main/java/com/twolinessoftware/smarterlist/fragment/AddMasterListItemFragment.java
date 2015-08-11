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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnMasterListItemAddedEvent;
import com.twolinessoftware.smarterlist.model.MasterSmartCategory;
import com.twolinessoftware.smarterlist.model.MasterSmartListItem;
import com.twolinessoftware.smarterlist.model.dao.MasterListItemDAO;
import com.twolinessoftware.smarterlist.service.MasterListService;
import com.twolinessoftware.smarterlist.util.Ln;

import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by John on 2015-04-09.
 */
public class AddMasterListItemFragment extends BaseFragment {

    private static final String EXTRA_MASTERLIST_NAME = "EXRTA_MASTER_LIST_NAME";
    private static final String EXTRA_HINT = "EXTRA_HINT";

    @InjectView(R.id.edit_master_name)
    EditText m_mastername;

    @InjectView(R.id.pull_down_categories)
    Spinner m_categorySpinner;


    @Inject
    MasterListItemDAO m_masterListItemDAO;

    @Inject
    MasterListService m_masterListService;

    @Inject
    protected Bus m_eventBus;

    private ActionMode m_actionMode;
    private String m_masterListName;
    private SimpleCategoryAdapter m_adapter;
    private String m_hint;


    public static AddMasterListItemFragment newInstance(String masterListName, String hint) {

        Bundle args = new Bundle();

        args.putString(EXTRA_MASTERLIST_NAME, masterListName);
        args.putString(EXTRA_HINT, hint);

        AddMasterListItemFragment f = new AddMasterListItemFragment();
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);

        m_masterListName = getArguments().getString(EXTRA_MASTERLIST_NAME);

        m_hint = getArguments().getString(EXTRA_HINT);

        setRetainInstance(true);

    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_newmasteritem;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureToolbar();
        m_masterListItemDAO.queryCategories(m_masterListName)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(m_categorySubscriber);
    }


    private Subscriber<List<MasterSmartCategory>> m_categorySubscriber = new Subscriber<List<MasterSmartCategory>>() {
        @Override
        public void onCompleted() {
            Ln.v("Categories loaded");
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(List<MasterSmartCategory> smartCategories) {
            updatePullDown(smartCategories);
        }
    };

    private void updatePullDown(List<MasterSmartCategory> categories) {

        m_adapter = new SimpleCategoryAdapter(getBaseActivity(), categories);
        m_categorySpinner.setAdapter(m_adapter);

        if (m_hint != null) {
            m_mastername.setText(m_hint);
            m_hint = null;
        }


    }


    public void setHint(String lastSearchQuery) {
        m_hint = lastSearchQuery;
    }


    private class SimpleCategoryAdapter extends ArrayAdapter<MasterSmartCategory> {

        private List<MasterSmartCategory> m_entries;


        public SimpleCategoryAdapter(Context context, List<MasterSmartCategory> categories) {
            super(context, R.layout.item_single_spinner, R.id.text_spinner_name, categories);
            m_entries = categories;
        }

        @Override
        public int getCount() {
            return m_entries.size();
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater(null);
                convertView = inflater.inflate(R.layout.item_single_spinner, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text_spinner_name);
                holder.sidebar = convertView.findViewById(R.id.view_colorsidebar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MasterSmartCategory category = getItem(position);

            holder.text.setText(category.getName());
            holder.sidebar.setBackgroundColor(category.getColor());

            return convertView;
        }

        @Override
        public MasterSmartCategory getItem(int position) {
            return m_entries.get(position);
        }

    }

    static class ViewHolder {
        TextView text;
        View sidebar;
    }

    private void save() {
        if (TextUtils.isEmpty(m_mastername.getText())) {
            m_mastername.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_warning), null, null, null);
        } else {

            String itemName = m_mastername.getText().toString();

            long categoryId = m_adapter.getItem(m_categorySpinner.getSelectedItemPosition()).getId();


            StringBuilder sb = new StringBuilder(itemName.toLowerCase());
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            String name = sb.toString();

            Ln.v("Creating new masterlistitem in database");

            MasterSmartListItem item = new MasterSmartListItem();
            item.setMasterListName(Constants.DEFAULT_MASTERLIST_NAME);
            item.setName(name);

            MasterSmartCategory category = m_masterListItemDAO.findCategory(categoryId);
            item.setCategoryId(category.getId());
            item.setCategoryName(category.getName());
            item.setCategoryColor(category.getColor());
            item.setCategoryIconUrl(category.getIconUrl());


            getBaseActivity().showLoadingDialog(getString(R.string.dialog_saving_item));

            m_masterListService.addNewMasterListItem(item)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(masterSmartListItem -> {
                        getBaseActivity().dismissDialog();
                        m_eventBus.post(new OnMasterListItemAddedEvent(masterSmartListItem));
                    }, error -> {
                        Ln.e("Error:" + error.getMessage());
                        getBaseActivity().dismissDialog();
                        getBaseActivity().showErrorCrouton(getString(R.string.error_cannot_create_item));
                    }, () -> {
                        m_actionMode.finish();
                    });

        }
    }

    private void configureToolbar() {

        m_actionMode = getToolbar().startActionMode(m_ActionModeCallback);
        m_actionMode.setTitle(R.string.master_add_title);
    }


    private ActionMode.Callback m_ActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.create_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_save:
                    save();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            m_actionMode = null;
            getFragmentManager().popBackStack();
        }
    };


}
