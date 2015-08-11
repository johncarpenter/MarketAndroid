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

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by John on 15/05/14.
 */
public class Constants {


    public static SimpleDateFormat getDateFormat()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat;
    }


    // Internal Shared Preferences
    public static final String SHARED_PREFERENCES_FILE = "com.twolinessoftware.smarterlist.SHARED_PREFERENCES";

    // Holds the current stored version of the masterlists *+name=version
    public static final String PREFERENCE_CURRENT_LIST_VERSION = "PREFERENCE_CURRENT_LIST_VERSION_";

    // DATE the last sync occured
    //public static final String PREFERENCE_LAST_SYNC_TIME = "PREFERENCE_LAST_SYNC";
    public static final String PREFERENCE_LAST_SYNC_TIME_SMARTLISTS = "PREFERENCE_LAST_SYNC_SMARTLISTS";

    // First launch show tutorial
    public static final String PREFERENCE_SHOWN_TUTORIAL = "PREFERENCES_SHOWN_TUTORIAL";


    //public static final String API_ENDPOINT = "http://192.168.1.47:8080/api";
    public static final String API_ENDPOINT = "https://api.smarterlistapp.com/api";

    public static String ANDROID_CLIENT_ID ="1KCE6YTF3BYUQ9L8CBNY4KT8Q";
    public static String ANDROID_CLIENT_SECRET ="NOTREQUIRED";
    public static final String USER_DATA_AUTH_TOKEN_EXPIRY = "USER_DATA_AUTH_EXPIRY";

    public static final String SMARTERLIST_ACCOUNT_TYPE = "com.twolinessoftware.smarterlist";
    public static final String SMARTERLIST_TOKEN_TYPE = "email";
    public static final String SMARTERLIST_SYNC_AUTHORITY = "com.twolinessoftware.smarterlist.masterlistitem";


    public static final String DEFAULT_MASTERLIST_NAME = "grocery_list";

    public static final String GCM_SENDER_ID = "507510369836";
    public static final String PREFERENCE_REG_ID = "PREFERENCES_REG_ID";
    public static final String PREFERENCE_APP_VERSION = "PREFERENCES_APP_VERSION";
}
