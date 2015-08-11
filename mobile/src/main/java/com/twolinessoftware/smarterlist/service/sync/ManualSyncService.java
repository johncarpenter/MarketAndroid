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

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.service.SmartListService;
import com.twolinessoftware.smarterlist.util.AccountUtils;
import com.twolinessoftware.smarterlist.util.Ln;

import javax.inject.Inject;

/**
 * Created by John on 2015-06-02.
 */
public class ManualSyncService extends GcmTaskService {

    @Inject
    SmartListService m_smartListService;

    @Inject
    AccountUtils m_accountUtils;

    private boolean hasToken;


    @Override
    public int onRunTask(TaskParams taskParams) {

        Injector.inject(this);

        Ln.v("Syncing Data");

        m_accountUtils.refreshAuthToken(new AccountUtils.TokenRefreshListener() {
            @Override
            public void onTokenRefreshed() {
                hasToken = true;
                m_smartListService.synchronizeSmartLists();
            }

            @Override
            public void onTokenError() {
                hasToken = false;
            }
        });

        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
