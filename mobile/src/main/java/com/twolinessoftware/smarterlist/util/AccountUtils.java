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

package com.twolinessoftware.smarterlist.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.OnAccountsUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.activity.LoginActivity;
import com.twolinessoftware.smarterlist.event.OnLogoutEvent;
import com.twolinessoftware.smarterlist.model.SmartList;
import com.twolinessoftware.smarterlist.model.Token;
import com.twolinessoftware.smarterlist.service.sync.ManualSyncService;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by John on 2015-04-02.
 */
public class AccountUtils implements OnAccountsUpdateListener {

    private final AccountManager m_accountManager;

    private final Context m_context;

    private final Bus m_eventBus;

    public boolean isOwner(SmartList item) {
        return getEmailAddress() != null && getEmailAddress().equalsIgnoreCase(item.getOwner());
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        if (getAccount() == null) {
            m_eventBus.post(new OnLogoutEvent());
        }
    }

    public void forceAuthToken(String authtoken) {
        String m_authToken = authtoken;
    }

    public interface TokenRefreshListener {
        void onTokenRefreshed();

        void onTokenError();
    }

    public AccountUtils(Context context, AccountManager accountManager, Bus eventBus) {
        this.m_context = context;
        this.m_accountManager = accountManager;
        // m_accountManager.addOnAccountsUpdatedListener(this, new Handler(), false);
        this.m_eventBus = eventBus;
    }


    public final static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    public static final String getHttpBearerAuthHeader(String token) {
        return "Bearer " + token;
    }

    public static final String getHttpBasicAuthHeader(String username, String password) {
        String digest = username + ":" + password;
        return "Basic " + Base64.encodeToString(digest.getBytes(), Base64.NO_WRAP);
    }


    public static final Intent generateAuthIntent(Token accessToken, String username, String password) {

        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.SMARTERLIST_ACCOUNT_TYPE);
        data.putString(AccountManager.KEY_AUTHTOKEN, accessToken.accessToken);
        data.putString(LoginActivity.KEY_ACCOUNT_PASSWORD, password);
        data.putBoolean(LoginActivity.EXTRA_IS_ADDING, true);


        final Intent res = new Intent();
        res.putExtras(data);
        return res;
    }

    public String getEmailAddress() {
        if (!isLoggedIn()) {
            return null;
        }

        return getAccount().name;
    }

    protected String getPassword() {
        if (!isLoggedIn()) {
            return null;
        }
        return m_accountManager.getPassword(getAccount());
    }


    public void invalidateToken() {
        if (getAuthToken() != null) {
            Ln.e("Authentication Token Invalidated");
            m_accountManager.invalidateAuthToken(Constants.SMARTERLIST_ACCOUNT_TYPE, getAuthToken());
        }
    }

    public void refreshAuthToken(final TokenRefreshListener listener) {

        if (getAccount() != null) {

            final AccountManagerFuture<Bundle> future = m_accountManager.getAuthToken(getAccount(), Constants.SMARTERLIST_TOKEN_TYPE, null, false, null, null);

            Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {
                    try {

                        Bundle bnd = future.getResult();

                        final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                        subscriber.onNext(authtoken);
                        subscriber.onCompleted();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            }).subscribeOn(Schedulers.newThread())
                    .subscribe(token -> {
                        if (token == null) {
                            Ln.e("Unable to retrieve token:null");
                            if (listener != null) {
                                listener.onTokenError();
                            }
                        }
                    }, error -> {
                        Ln.e("Unable to retrieve token");
                        if (listener != null) {
                            listener.onTokenError();
                        }
                    }, () -> {
                        if (listener != null) {
                            listener.onTokenRefreshed();
                        }
                    });

        }
    }

    public void refreshAuthToken() {

        refreshAuthToken(null);
    }

    public String getAuthToken() {
        return m_accountManager.peekAuthToken(getAccount(), Constants.SMARTERLIST_TOKEN_TYPE);
    }

    public boolean isLoggedIn() {
        return getAccount() != null;
    }

    public Account getAccount() {
        Account[] accounts = m_accountManager.getAccountsByType(Constants.SMARTERLIST_ACCOUNT_TYPE);
        return accounts.length > 0 ? accounts[0] : null;
    }


    public void scheduleSmartlistSync() {

        GcmNetworkManager gcm = GcmNetworkManager.getInstance(m_context);

        OneoffTask syncTask = new OneoffTask.Builder()
                .setService(ManualSyncService.class)
                .setExecutionWindow(5, 30)
                .setTag("sync-smartlist")
                .setUpdateCurrent(true)
                .setRequiredNetwork(Task.NETWORK_STATE_ANY)
                .build();

        gcm.schedule(syncTask);
    }

    public void removeAccount() {

        Ln.v("Removing account");
        //  m_accountManager.removeOnAccountsUpdatedListener(this);

        Account account = getAccount();
        m_accountManager.invalidateAuthToken(Constants.SMARTERLIST_ACCOUNT_TYPE, getAuthToken());
        m_accountManager.clearPassword(account);

        m_accountManager.removeAccount(account, null, null);


    }


}
