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

import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.view.inputmethod.InputMethodManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Module for all Android related provisions
 */
@Module(complete = false, library = true)
public class AndroidModule {

    @Provides
    @Singleton
    Context provideAppContext() {
        return SmarterListApplication.getInstance().getApplicationContext();
    }


    @Provides
    SharedPreferences provideDefaultSharedPreferences(final Context context) {
        return context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE,Context.MODE_PRIVATE);
    }

    @Provides
    ConnectivityManager provideConnectivityManager(final Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Provides
    SearchManager provideSearchManager(final Context context){
        return (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
    }

    @Provides
    AccountManager provideAccountManager(final Context context){
        return (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
    }

    @Provides
    InputMethodManager provideInputMethodManager(final Context context){
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

}