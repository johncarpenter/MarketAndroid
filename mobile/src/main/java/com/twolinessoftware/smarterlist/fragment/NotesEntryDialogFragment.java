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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.model.SmartListItem;
import com.twolinessoftware.smarterlist.model.dao.SmartListItemDAO;
import com.twolinessoftware.smarterlist.util.AccountUtils;
import com.twolinessoftware.smarterlist.util.Ln;

import java.util.Date;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by John on 2015-05-21.
 */
public class NotesEntryDialogFragment extends DialogFragment {

    private static final String EXTRA_SMARTLIST_ITEM_ID = "EXTRA_SMARTLIST_ITEM_ID";

    @Inject
    SmartListItemDAO m_smartListItemDao;

    @InjectView(R.id.edit_notes)
    EditText m_editNotes;

    @Inject
    AccountUtils m_accountUtils;

    private SmartListItem m_smartListItem;

    public static NotesEntryDialogFragment newInstance(long smartListItemId){

        Bundle args = new Bundle();

        args.putLong(EXTRA_SMARTLIST_ITEM_ID,smartListItemId);
        NotesEntryDialogFragment f =  new NotesEntryDialogFragment();
        f.setArguments(args);

        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injector.inject(this);

        m_smartListItem = m_smartListItemDao.findByItemId(getArguments().getLong(EXTRA_SMARTLIST_ITEM_ID));



    }

  /*  @Override
    protected int setContentView() {
        return R.layout.fragment_noteentry;
    }
*/

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater li = LayoutInflater.from(getActivity());
        View noteView = li.inflate(R.layout.fragment_noteentry, null);

        ButterKnife.inject(this,noteView);

        m_editNotes.setText(m_smartListItem.getNotes());

        builder.setView(noteView);

        builder.setCancelable(true)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onAdd(null);
                    }
                });


        return builder.create();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }


    //@OnClick(R.id.button_cancel)
    public void onCancel(View view){
        dismiss();
    }

    //@OnClick(R.id.button_add)
    public void onAdd(View view){
       String notes = m_editNotes.getText().toString().trim();
       Ln.v("Updating Notes to:" + notes);

        m_smartListItem.setNotes(notes);
        m_smartListItem.setLastModified(new Date());
        m_smartListItemDao.save(m_smartListItem);

        m_accountUtils.scheduleSmartlistSync();

        dismiss();
    }

}
