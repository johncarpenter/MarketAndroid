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
import android.util.Log;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.event.OnCommunicationStatusEvent;
import com.twolinessoftware.smarterlist.event.OnErrorEvent;
import com.twolinessoftware.smarterlist.model.MasterSmartList;
import com.twolinessoftware.smarterlist.model.MasterSmartListItem;
import com.twolinessoftware.smarterlist.model.dao.MasterListItemDAO;
import com.twolinessoftware.smarterlist.model.dao.MasterSmartListDAO;
import com.twolinessoftware.smarterlist.model.dao.SmartListDAO;
import com.twolinessoftware.smarterlist.util.Ln;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class MasterListService extends BaseCommunicationService {

    @Inject
    MasterListItemDAO m_masterListItemDAO;

    @Inject
    MasterSmartListDAO m_masterSmartListDAO;

    @Inject
    SmartListDAO m_smartListDAO;

    @Inject
    SharedPreferences m_preferences;


    @Inject
    Bus m_eventBus;

    private final MasterListApi m_api;

    private int m_serverVersion = 0;


    public MasterListService(Context context) {

        super(context);

        Injector.inject(this);

        m_api = m_restAdapter.create(MasterListApi.class);
    }

    /**
     * GET /masterlist/list and update all subsequent lists if necessary
     *
     *
     */
    public void synchronizeMasterLists(){

        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.PROGRESS));

        synchronizeNewMasterlistItems();

        getMasterListsObservable()
                .doOnNext(count ->Ln.v("Received "+count+" master list(s)"))
                .flatMap(count ->getMasterListsNeedingUpdate(false))
                .flatMap(masterSmartLists -> Observable.from(masterSmartLists))
                .doOnNext(masterSmartList -> Ln.v("Synchronizing "+masterSmartList.getName()+" list"))
                .flatMap(masterSmartList -> getMasterListToSync(masterSmartList))
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.COMPLETED));
                        Ln.v("Sync completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE));
                        handleErrors(e);
                        Ln.e("Unable to update master lists:" + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(Integer numItems) {
                        Ln.v("Synchronized " + numItems + " items");
                    }
                });


    }


     public void synchronizeNewMasterlistItems(){

       getMasterListItemsThatNeedSync()
                .flatMap(masterSmartListItems -> Observable.from(masterSmartListItems))
                .doOnNext(masterSmartListItem -> Ln.v("Pushing new item " + masterSmartListItem.getName() + " to server"))
                .flatMap(masterSmartListItem -> addNewMasterListItem(masterSmartListItem))
               .subscribeOn(Schedulers.io())
               .subscribe(new Subscriber<MasterSmartListItem>() {
                   @Override
                   public void onCompleted() {
                       Ln.v("Sync completed");
                   }

                   @Override
                   public void onError(Throwable e) {
                       Ln.e("Unable to update master lists:" + Log.getStackTraceString(e));
                   }

                   @Override
                   public void onNext(MasterSmartListItem numItems) {
                       Ln.v("Synchronized MasterItem + " + numItems.getName());
                   }
               });

    }




    public void checkAndSynchronizeMasterList(String listName, boolean force){
        MasterSmartList masterSmartList = m_masterSmartListDAO.findByName(listName);

        int currentVersion = m_preferences.getInt(Constants.PREFERENCE_CURRENT_LIST_VERSION+listName,0);
        int version = masterSmartList.getVersion();

        Ln.v("Manual update check for " + listName + " version:" + currentVersion + " vs Server:" + version+" force:"+force);

        if(version > currentVersion || force){
            syncMasterList(masterSmartList);
        }
    }

    public Observable<MasterSmartListItem> addNewMasterListItem(MasterSmartListItem item){
       return m_api.addNewMasterlistItem(item.getMasterListName(),new MasterListItemPostWrapper(item.getName(), item.getDescription(), item.getCategoryId()))
               .map(m_masterListItemDAO::saveAndReturn);

    }

    private Observable<List<MasterSmartListItem>> getMasterListItemsThatNeedSync(){
        return  m_masterListItemDAO.getListOfCustomItemsThatNeedSync();
    }


    private Observable<Integer> getMasterListsObservable() {
        return m_api.getMasterLists()
                .flatMap(masterlists -> m_masterSmartListDAO.bulkReplaceAndInsert(masterlists));
    }

    private Observable<List<MasterSmartList>> getMasterListsNeedingUpdate(boolean force){
        return  m_masterSmartListDAO.getListOfMasterListsForSyncing(force);
    }

    private Observable<Integer> getMasterListToSync(MasterSmartList masterSmartList){
        return getMasterListItems(masterSmartList.getName())
                .map(items -> appendListName(items,masterSmartList.getName()))
                .flatMap(items -> m_masterListItemDAO.bulkReplaceAndInsert(items))
                .map(count -> {
                    m_preferences.edit().putInt(Constants.PREFERENCE_CURRENT_LIST_VERSION + masterSmartList.getName(), masterSmartList.getVersion()).commit();
                    return count;
                });
    }


    private void syncMasterList(final MasterSmartList masterSmartList) {

        Ln.v("Syncing List Items for "+masterSmartList.getName());
        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.PROGRESS));

        getMasterListToSync(masterSmartList)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.COMPLETED));
                        Ln.v("Sync completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE));
                        m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.COMMUNICATION));
                        Ln.e("Unable to update master list:" + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Ln.v("Synchronized " + integer + " items");
                    }
                });

    }

    private List<MasterSmartListItem> appendListName(List<MasterSmartListItem> items, String listName) {

        for(int i=0;i<items.size();i++){
            MasterSmartListItem item = items.get(i);

            item.setMasterListName(listName);
            items.set(i, item);
        }
        return items;
    }

    private void syncVersionCheckOnline(final String listName){
        getMasterListVersion(listName)
                .map(version -> checkOnlineVersion(listName, version))
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Boolean>() {
                               @Override
                               public void onCompleted() {

                               }

                               @Override
                               public void onError(Throwable e) {
                                   Ln.e("Unable to check version:" + e);
                               }

                               @Override
                               public void onNext(Boolean requiresUpdate) {
                                 //  if (requiresUpdate)
                                 //      syncMasterList(listName,m_serverVersion);
                               }
                           }
                );
    }



    private boolean checkOnlineVersion(String listname, Integer version) {

        int currentVersion = m_preferences.getInt(Constants.PREFERENCE_CURRENT_LIST_VERSION+listname,0);

        Ln.v("Checking version:" + currentVersion + " vs Server:" + version);

        m_serverVersion = version;

        return (version > currentVersion);

    }


    private Observable<Integer> getMasterListVersion(String listName){
        return m_api.getMasterListVersion(listName).map(versionWrapper ->versionWrapper.getVersion());
    }

    private Observable<List<MasterSmartListItem>> getMasterListItems(String masterListName){
        return m_api.getItemsInList(masterListName);
    }

    public static class MasterListItemPostWrapper{

        String name;
        String description;
        long categoryId;

        public MasterListItemPostWrapper(String name, String description, long categoryId) {
            this.name = name;
            this.description = description;
            this.categoryId = categoryId;
        }

        public MasterListItemPostWrapper() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(long categoryId) {
            this.categoryId = categoryId;
        }
    }


}
