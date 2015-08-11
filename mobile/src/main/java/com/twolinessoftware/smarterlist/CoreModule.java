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
import android.content.Context;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.activity.LoginActivity;
import com.twolinessoftware.smarterlist.activity.MainNavigationActivity;
import com.twolinessoftware.smarterlist.activity.PlanViewPagerActivity;
import com.twolinessoftware.smarterlist.activity.SearchActivity;
import com.twolinessoftware.smarterlist.activity.ShoppingNavigationActivity;
import com.twolinessoftware.smarterlist.fragment.AddMasterListItemFragment;
import com.twolinessoftware.smarterlist.fragment.CreateSmartListFragment;
import com.twolinessoftware.smarterlist.fragment.LoginFragment;
import com.twolinessoftware.smarterlist.fragment.MasterCategoryListViewRecyclerViewFragment;
import com.twolinessoftware.smarterlist.fragment.MasterListDialogFragment;
import com.twolinessoftware.smarterlist.fragment.MasterListPredictedRecyclerViewFragment;
import com.twolinessoftware.smarterlist.fragment.MasterListSearchRecyclerViewFragment;
import com.twolinessoftware.smarterlist.fragment.MasterListViewRecyclerViewFragment;
import com.twolinessoftware.smarterlist.fragment.MasterListViewSearchFragment;
import com.twolinessoftware.smarterlist.fragment.NotesEntryDialogFragment;
import com.twolinessoftware.smarterlist.fragment.RegisterFragment;
import com.twolinessoftware.smarterlist.fragment.ResetPasswordFragment;
import com.twolinessoftware.smarterlist.fragment.SharesListViewRecyclerViewFragment;
import com.twolinessoftware.smarterlist.fragment.SmartItemListViewRecyclerViewFragment;
import com.twolinessoftware.smarterlist.fragment.SmartListCardViewRecyclerViewFragment;
import com.twolinessoftware.smarterlist.fragment.SmartListRecyclerViewFragment;
import com.twolinessoftware.smarterlist.model.dao.MasterListItemDAO;
import com.twolinessoftware.smarterlist.model.dao.MasterSmartListDAO;
import com.twolinessoftware.smarterlist.model.dao.SmartListDAO;
import com.twolinessoftware.smarterlist.model.dao.SmartListItemDAO;
import com.twolinessoftware.smarterlist.model.provider.MasterSmartItemProvider;
import com.twolinessoftware.smarterlist.model.provider.MasterSmartListProvider;
import com.twolinessoftware.smarterlist.model.provider.SmartItemProvider;
import com.twolinessoftware.smarterlist.model.provider.SmartListProvider;
import com.twolinessoftware.smarterlist.receiver.PushReceiver;
import com.twolinessoftware.smarterlist.service.AccountAuthenticatorService;
import com.twolinessoftware.smarterlist.service.AccountService;
import com.twolinessoftware.smarterlist.service.GoogleServices;
import com.twolinessoftware.smarterlist.service.MasterListService;
import com.twolinessoftware.smarterlist.service.SmartListService;
import com.twolinessoftware.smarterlist.service.sync.ManualSyncService;
import com.twolinessoftware.smarterlist.util.AccountUtils;
import com.twolinessoftware.smarterlist.util.PostFromAnyThreadBus;
import com.twolinessoftware.smarterlist.view.MasterSmartListCategoryRecyclerViewAdapter;
import com.twolinessoftware.smarterlist.view.MasterSmartListItemRecyclerViewAdapter;
import com.twolinessoftware.smarterlist.view.MasterSmartListItemSearchAdapter;
import com.twolinessoftware.smarterlist.view.MasterSmartListRecyclerViewAdapter;
import com.twolinessoftware.smarterlist.view.SharesListRecyclerViewAdapter;
import com.twolinessoftware.smarterlist.view.SmartListCardRecyclerViewAdapter;
import com.twolinessoftware.smarterlist.view.SmartListItemRecyclerViewAdapter;
import com.twolinessoftware.smarterlist.view.SmartListRecyclerViewAdapter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        injects = {
                // Main App
                SmarterListApplication.class,
                // Activities
                LoginActivity.class,
                MainNavigationActivity.class,
                PlanViewPagerActivity.class,
                ShoppingNavigationActivity.class,
                SearchActivity.class,
                // Services
                AccountAuthenticatorService.class,
                ManualSyncService.class,
                // Receivers
                PushReceiver.class,
                //Fragments
                LoginFragment.class,
                RegisterFragment.class,
                ResetPasswordFragment.class,
                MasterListViewRecyclerViewFragment.class,
                MasterCategoryListViewRecyclerViewFragment.class,
                SmartListCardViewRecyclerViewFragment.class,
                SmartItemListViewRecyclerViewFragment.class,
                CreateSmartListFragment.class,
                MasterListDialogFragment.class,
                MasterListViewSearchFragment.class,
                SharesListViewRecyclerViewFragment.class,
                AddMasterListItemFragment.class,
                MasterListSearchRecyclerViewFragment.class,
                MasterListPredictedRecyclerViewFragment.class,
                NotesEntryDialogFragment.class,
                SmartListRecyclerViewFragment.class,
                // Utility Classes
                MasterSmartListItemRecyclerViewAdapter.class,
                MasterSmartListCategoryRecyclerViewAdapter.class,
                MasterSmartListRecyclerViewAdapter.class,
                SmartListCardRecyclerViewAdapter.class,
                SmartListItemRecyclerViewAdapter.class,
                MasterSmartListItemSearchAdapter.class,
                SharesListRecyclerViewAdapter.class,
                SmartListRecyclerViewAdapter.class,
                // Singleton services
                MasterListService.class,
                AccountService.class,
                SmartListService.class,
                GoogleServices.class,
                // DAOs
                MasterSmartListDAO.class,
                SmartListItemDAO.class
        }
)
public class CoreModule {

    @Singleton
    @Provides
    AccountUtils providesAccountUtils(final Context context,final AccountManager accountManager, final Bus eventBus) {
        return new AccountUtils(context,accountManager,eventBus);
    }

    @Singleton
    @Provides
    Bus providesOttoBus() {
        return new PostFromAnyThreadBus();
    }

    @Singleton
    @Provides
    MasterListService providesMasterListService(final Context context) {return new MasterListService(context);}

    @Singleton
    @Provides
    SmartListService providesSmartListService(final Context context) {return new SmartListService(context);}

    @Singleton
    @Provides
    AccountService providesAccountService(final Context context) {return new AccountService(context);}

    @Singleton
    @Provides
    GoogleServices providesGoogleServices(final Context context) {return new GoogleServices(context);}


    @Singleton
    @Provides
    MasterListItemDAO provideMasterListItemDao(final Context context){ return new MasterListItemDAO(context, new MasterSmartItemProvider());}

    @Singleton
    @Provides
    MasterSmartListDAO provideMasterSmartListDao(final Context context){ return new MasterSmartListDAO(context, new MasterSmartListProvider());}

    @Singleton
    @Provides
    SmartListItemDAO provideSmartListItemDao(final Context context){ return new SmartListItemDAO(context, new SmartItemProvider());}


    @Singleton
    @Provides
    SmartListDAO provideSmartListDao(final Context context){ return new SmartListDAO(context, new SmartListProvider());}

}