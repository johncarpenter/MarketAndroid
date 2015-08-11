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

package com.twolinessoftware.smarterlist.service.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.service.SmartListService;
import com.twolinessoftware.smarterlist.util.AccountUtils;

import javax.inject.Inject;


public class MasterListSyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject
    SmartListService m_smartListService;

    @Inject
    AccountUtils m_accountUtils;



    /**
     * Set up the sync adapter
     */
    public MasterListSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        init(context);
    }

    public MasterListSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        init(context);
    }

    private void init(Context context){

        Injector.inject(this);

     }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {


       /* Ln.v("Refreshing Data Stream");
        m_accountUtils.refreshAuthToken(new AccountUtils.TokenRefreshListener() {
            @Override
            public void onTokenRefreshed() {
                Ln.v("Synchronizing with Server");
                m_smartListService.synchronizeSmartLists();
            }
        });*/





    }





}