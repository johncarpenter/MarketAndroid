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

package com.twolinessoftware.smarterlist.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.event.OnAccountLoggedInEvent;
import com.twolinessoftware.smarterlist.event.OnAccountPasswordResetEvent;
import com.twolinessoftware.smarterlist.event.OnCommunicationStatusEvent;
import com.twolinessoftware.smarterlist.event.OnErrorEvent;
import com.twolinessoftware.smarterlist.event.OnLogoutEvent;
import com.twolinessoftware.smarterlist.model.Token;
import com.twolinessoftware.smarterlist.util.AccountUtils;
import com.twolinessoftware.smarterlist.util.Ln;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AccountService extends BaseCommunicationService{

    @Inject
    Bus m_eventBus;

    @Inject
    SharedPreferences m_sharedPreferences;


    private final AccountApi m_api;

    public AccountService(Context context) {

        super(context);

        m_api = m_restAdapter.create(AccountApi.class);
    }



    public rx.Observable<ApiResponse> updateGcm(final String registrationId){
       return m_api.updateGcm(registrationId);
    }


    public Token getAccessToken(final String username, final String password){
        return m_api.getTokenSync("password", username, password, Constants.ANDROID_CLIENT_ID, Constants.ANDROID_CLIENT_SECRET, "*");
    }

    public void resetPassword(final String email){
        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.PROGRESS));

        m_api.resetPassword(email)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE));
                    }

                    @Override
                    public void onError(Throwable e) {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE));

                        if(e instanceof ApiException){
                            ApiException ex = (ApiException) e;
                            handleErrors(ex);
                        }else{
                            m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.COMMUNICATION));
                        }
                    }

                    @Override
                    public void onNext(ApiResponse account) {
                        Ln.v("Account reset email sent:"+email);
                        m_eventBus.post(new OnAccountPasswordResetEvent());
                    }
                });
    }


    public void registerUser(final String username, final String password){
        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.PROGRESS));

        m_api.registerUser(username, password)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Token>() {
                    @Override
                    public void onCompleted() {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.COMPLETED));
                    }

                    @Override
                    public void onError(Throwable e) {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.COMPLETED));

                        if(e instanceof ApiException){
                            ApiException ex = (ApiException) e;
                            handleErrors(ex);
                        }else{
                            m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.COMMUNICATION));
                        }
                    }

                    @Override
                    public void onNext(Token token) {
                        Ln.v("Account logged in:"+username);
                        m_eventBus.post(new OnAccountLoggedInEvent(AccountUtils.generateAuthIntent(token,username,password),true));

                    }
                });
    }

    public void loginUser(final String username, final String password){
        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.PROGRESS));

        m_api.getToken("password", username, password, Constants.ANDROID_CLIENT_ID, Constants.ANDROID_CLIENT_SECRET, "*")
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Token>() {
                    @Override
                    public void onCompleted() {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE));
                    }

                    @Override
                    public void onError(Throwable e) {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE));
                        m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.LOGIN_ERROR));
                        Ln.e("Unable to login user:" + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(Token token) {
                        m_eventBus.post(new OnAccountLoggedInEvent(AccountUtils.generateAuthIntent(token,username,password),false));

                    }
                });
    }

    @Override
    public void onStart() {
        Ln.v("Starting Accounts Services Singleton");
        m_eventBus.register(this);
    }

    @Override
    public void onStop() {
        Ln.v("Stopping Accounts Services Singleton");
        m_eventBus.unregister(this);
    }

    @Subscribe
    public void onLogoutEvent(OnLogoutEvent event) {

        Ln.v("Logging out");
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                m_accountUtils.removeAccount();
                m_sharedPreferences.edit().clear().apply();
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        }).observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(pass -> {
                    Ln.v("Data Cleaned");
                }, error -> {
                    Ln.e("Unable to logout:Cause:"+Log.getStackTraceString(error));
                }, () -> {
                    Ln.v("Restarting Main Activity");
                    Intent i = m_context.getPackageManager()
                            .getLaunchIntentForPackage( m_context.getPackageName() );
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    m_context.startActivity(i);
                });

    }

    @Produce
    public OnCommunicationStatusEvent produceInitialCommunicationStatus(){
        Ln.v("Sending comm status");
        if (isNetworkAvailable()){
            return new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE);
        }else{
            return new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.NOT_AVAILABLE);
        }
    }
}
