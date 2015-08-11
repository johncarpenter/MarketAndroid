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
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.util.AccountUtils;
import com.twolinessoftware.smarterlist.util.Ln;

import java.io.IOException;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by John on 18/05/14.
 */
public class GoogleServices implements SingletonService{

    private final Context m_context;

    @Inject
    SharedPreferences prefs;

    @Inject
    AccountUtils m_accountUtils;

    @Inject
    AccountService m_accountService;


    private GoogleCloudMessaging m_gcm;

    private String m_registrationId;


    public GoogleServices(Context application) {
        this.m_context = application;

        Injector.inject(this);
        if(checkPlayServices()) {
            m_gcm = GoogleCloudMessaging.getInstance(m_context);
            launchAnalytics(application);
        }

    }

    private void register(){
        // Don't bother registering GCM unless an account is active
        if (m_accountUtils.isLoggedIn() && checkPlayServices()) {
            m_gcm = GoogleCloudMessaging.getInstance(m_context);

            m_registrationId = getRegistrationId();

            if (TextUtils.isEmpty(m_registrationId)) {
                registerGcm();
            }
        }
    }

    public void registerGcm() {

        if(m_gcm == null){
            Ln.v("Google Play Services not enabled for this device");
            return;
        }

        Observable<String> registerGCM = Observable.create(subscriber -> {
            try {
                m_registrationId = m_gcm.register(Constants.GCM_SENDER_ID);
            } catch (IOException e) {
                subscriber.onError(e);
            }
            subscriber.onNext(m_registrationId);
        });

        registerGCM
                .flatMap(id-> m_accountService.updateGcm(id))
                .map(response->{
                    storeRegistrationId(m_registrationId);
                    return response.getMessage();
                }).subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Ln.v("GCM Updated");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Ln.e("Unable to update GCM: Cause:"+e.getMessage());
                    }

                    @Override
                    public void onNext(String r) {

                    }
                });

    }

    private void storeRegistrationId(String regId) {

        int appVersion = getAppVersion(m_context);

        Ln.v( "Saving regId on app version " + appVersion);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREFERENCE_REG_ID, regId);
        editor.putInt(Constants.PREFERENCE_APP_VERSION, appVersion);
        editor.apply();
    }




    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(m_context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private String getRegistrationId() {

        String registrationId = prefs.getString(Constants.PREFERENCE_REG_ID, "");
        if (TextUtils.isEmpty(registrationId)) {
            Ln.v( "Registration not found.");
            return null;
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(Constants.PREFERENCE_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(m_context);
        if (registeredVersion != currentVersion) {
            Ln.v( "App version changed.");
            return null;
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private Tracker m_tracker;

    private GoogleAnalytics m_analytics;

    private void launchAnalytics(Context context) {
        m_analytics = GoogleAnalytics.getInstance(context);
        m_analytics.setLocalDispatchPeriod(30);

        m_tracker = m_analytics.newTracker("UA-40831615-3");
        m_tracker.enableExceptionReporting(true);
        m_tracker.enableAdvertisingIdCollection(false);
        m_tracker.enableAutoActivityTracking(true);



    }

    public Tracker getTracker(){
        return m_tracker;
    }


    @Override
    public void onStart() {
        register();
    }

    @Override
    public void onStop() {

    }
}
