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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.BuildConfig;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.event.OnErrorEvent;
import com.twolinessoftware.smarterlist.util.AccountUtils;
import com.twolinessoftware.smarterlist.util.GsonUtils;
import com.twolinessoftware.smarterlist.util.Ln;

import javax.inject.Inject;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedByteArray;

/**
 * Created by John on 2015-04-03.
 */
public abstract class BaseCommunicationService implements SingletonService {

    private String m_authToken;

    public enum Status {ACTIVE, ARCHIVED}

    @Inject
    AccountUtils m_accountUtils;

    @Inject
    Bus m_eventBus;

    @Inject
    ConnectivityManager m_connectivityManager;

    final RequestInterceptor apiRequestInterceptor = request -> {
        request.addHeader("Accept", "application/json");
        // get username password
        if (m_accountUtils.isLoggedIn() && m_accountUtils.getAuthToken() != null) {
            request.addHeader("Authorization", AccountUtils.getHttpBearerAuthHeader(m_accountUtils.getAuthToken()));
        }
    };

    protected final RestAdapter m_restAdapter;
    protected final Context m_context;

    protected BaseCommunicationService(Context context) {

        Injector.inject(this);

        this.m_context = context;

        Gson gson = GsonUtils.buildGsonAdapter();

        ErrorHandler apiErrorConverter = cause -> {

            Ln.e("Comms Error:" + cause.getMessage());
            String json = new String(((TypedByteArray) cause.getResponse()
                    .getBody()).getBytes());
            Ln.v("Error:Cause:" + json);

            if (cause.getResponse() == null) {
                return new ApiException(ApiError.API_ERROR_SERVICE_NOT_AVAILABLE);
            }


            switch (cause.getResponse().getStatus()) {
                case 401:
                    m_accountUtils.invalidateToken();
                    m_accountUtils.refreshAuthToken();
                    return new ApiException(ApiError.API_ERROR_PERMISSION);

            }
            ApiError error = null;
            try {
                error = (ApiError) cause.getBodyAs(ApiError.class);
            } catch (Exception e) {
                Ln.v("Error getting code as json:" + e.getMessage());
                return new ApiException(ApiError.API_ERROR_SERVICE_NOT_AVAILABLE);
            }

            if (error == null) {
                Ln.e("Unknown error:" + cause.getResponse().getBody());
                return new ApiException(ApiError.API_ERROR_SERVICE_NOT_AVAILABLE);
            }


            return new ApiException(error);

        };
        m_restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.API_ENDPOINT)
                .setRequestInterceptor(apiRequestInterceptor)
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setErrorHandler(apiErrorConverter)
                .setConverter(new GsonConverter(gson))
                .build();
    }


    public void handleErrors(Throwable e) {
        if (e instanceof ApiException) {
            ApiException ex = (ApiException) e;
            switch (ex.getError().getCode()) {
                case 1:
                    m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.UNKNOWN_SERVER));
                    break;
                case 2:
                    m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.AUTHENTICATION));
                    break;
                case 3:
                    m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.REQUIRES_LOGIN));
                    break;
                case 202:
                    m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.REGISTER_EMAIL_TAKEN));
                    break;
                case 203:
                    m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.REGISTER_EMAIL_INVALID));
                    break;
                default:
                    m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.COMMUNICATION));
            }
        }
    }


    protected boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = m_connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onStart() {
        //     m_eventBus.register(this);
    }

    @Override
    public void onStop() {
        //   m_eventBus.unregister(this);
    }
}
