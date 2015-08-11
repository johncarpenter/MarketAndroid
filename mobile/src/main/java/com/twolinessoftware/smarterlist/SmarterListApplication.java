/*
 * Copyright (c) 2015. 2Lines Software,Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twolinessoftware.smarterlist;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;

import com.twolinessoftware.smarterlist.service.AccountService;
import com.twolinessoftware.smarterlist.service.GoogleServices;
import com.twolinessoftware.smarterlist.service.MasterListService;
import com.twolinessoftware.smarterlist.service.SmartListService;
import com.twolinessoftware.smarterlist.util.AccountUtils;
import com.twolinessoftware.smarterlist.util.Ln;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by John on 2014-11-28.
 */
public class SmarterListApplication extends Application {

    private static SmarterListApplication instance;

    @Inject
    MasterListService m_masterListService;

    @Inject
    SmartListService m_smartListService;


    @Inject
    AccountService m_accountService;

    @Inject
    AccountUtils m_accountUtils;


    @Inject
    GoogleServices m_googleServices;



    /**
     * Create main application
     */
    public SmarterListApplication() {
    }

    /**
     * Create main application
     *
     * @param context
     */
    public SmarterListApplication(final Context context) {
        this();
        attachBaseContext(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // Perform injection
        Injector.init(getRootModule(), this);


        // Startup Functions
       performStartup();

    }


    public void performStartup(){
        Observable.merge(
                initSingletonServiceLifecycle(),
                revalidateAuthToken(),
                initCheckForListUpdates())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<StartupHolder>() {
                    @Override
                    public void onCompleted() {
                        Ln.v("Startup Completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Ln.e("Error starting app:" + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(StartupHolder holder) {
                        Ln.v("Started " + holder.function+":success:"+holder.success);
                    }
                });
    }


    public void performShutdown(){
        stopSingletonServices();
    }


    private Observable<StartupHolder> revalidateAuthToken(){
        return Observable.create(sub ->{
            m_accountUtils.refreshAuthToken(new AccountUtils.TokenRefreshListener(){

                @Override
                public void onTokenRefreshed() {
                    sub.onNext(new StartupHolder("revalidateAuthToken",true));
                    sub.onCompleted();
                }

                @Override
                public void onTokenError() {
                    sub.onNext(new StartupHolder("revalidateAuthToken",false));
                    sub.onCompleted();

                }
            });

        });
    }


    private Observable<StartupHolder> initSingletonServiceLifecycle(){
        return Observable.create(sub ->{
            startSingletonServices();
            sub.onNext(new StartupHolder("initSingletonServiceLifecycle",true));
            sub.onCompleted();
        });
    }

    private Observable<StartupHolder> initCheckForListUpdates() {
        return Observable.create(sub-> {
                   m_masterListService.synchronizeMasterLists();
                   sub.onNext(new StartupHolder("initCheckForListUpdates", true));
                   sub.onCompleted();
                }
        );
    }

    private Object getRootModule() {
        return new RootModule();
    }

    private void startSingletonServices() {
        Ln.v("Starting up singletons");
        m_accountService.onStart();
        m_masterListService.onStart();
        m_smartListService.onStart();
        m_googleServices.onStart();
    }


    private void stopSingletonServices() {
        Ln.v("Shutting down singletons");
        m_masterListService.onStop();
        m_smartListService.onStop();
        m_accountService.onStop();
        m_googleServices.onStop();
    }

    /**
     * Create main application
     *
     * @param instrumentation
     */
    public SmarterListApplication(final Instrumentation instrumentation) {
        this();
        attachBaseContext(instrumentation.getTargetContext());
    }

    public static SmarterListApplication getInstance() {
        return instance;
    }

    private static class StartupHolder{
        String function;
        boolean success;

        public StartupHolder(String function, boolean success) {
            this.function = function;
            this.success = success;
        }
    }
}