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

import com.google.common.eventbus.Subscribe;
import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.event.OnCommunicationStatusEvent;
import com.twolinessoftware.smarterlist.event.OnLogoutEvent;
import com.twolinessoftware.smarterlist.event.OnSmartListCreatedEvent;
import com.twolinessoftware.smarterlist.model.MasterSmartListItem;
import com.twolinessoftware.smarterlist.model.ShareGroup;
import com.twolinessoftware.smarterlist.model.ShareToken;
import com.twolinessoftware.smarterlist.model.SmartList;
import com.twolinessoftware.smarterlist.model.SmartListItem;
import com.twolinessoftware.smarterlist.model.dao.MasterListItemDAO;
import com.twolinessoftware.smarterlist.model.dao.SmartListDAO;
import com.twolinessoftware.smarterlist.model.dao.SmartListItemDAO;
import com.twolinessoftware.smarterlist.util.Ln;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class SmartListService extends BaseCommunicationService {

    @Inject
    SharedPreferences m_prefs;

    @Inject
    Bus m_eventBus;

    @Inject
    SmartListDAO m_smartListDAO;

    @Inject
    SmartListItemDAO m_smartListItemDAO;

    @Inject
    MasterListItemDAO m_masterListItemDAO;



    private final SmartListApi m_api;


    public SmartListService(Context context) {
        super(context);

        Injector.inject(this);

        m_api = m_restAdapter.create(SmartListApi.class);
    }

    //@todo this might be slow? Should it be cached?
    public boolean containsItem(long masterItemId, long smartListId){
       return m_smartListItemDAO.findFirstByCriteria(null, "status = 'ACTIVE' and masterItemId=" + masterItemId + " and smartListId = "+smartListId, null, null) != null;
    }


    private Observable<Integer> pushChangesToServer(Date after){

        Ln.v("Starting pushChangesToServer from:"+Constants.getDateFormat().format(after));

        // This updates the list of changes locally
        return m_smartListDAO.getListOfAllChangedSmartLists(after)
                .flatMap(updateList -> Observable.from(updateList))
                .doOnNext(smartList -> Ln.v("Pushing changed smartlist " + smartList.getItemId() ))
                .flatMap(smartList -> pushSmartListChanges(smartList, after));

    }

    private Observable<Integer> pushSmartListChanges(final SmartList smartList, Date after){

        boolean isNewSmartList = smartList.getItemId() == smartList.getLocalId();

        Observable<SmartList> smartListServerUpdate = (isNewSmartList)?m_api.createSmartlist(smartList):m_api.updateSmartList(smartList.getItemId(),smartList);

        return smartListServerUpdate
                // update all the changed items
                .flatMap(count -> m_smartListItemDAO.getListOfChangedSmartListItems(smartList.getItemId(), after))
                .doOnNext(smartListItems -> Ln.v("Pushing Smartlist Items for list " + smartList.getItemId()))
                .flatMap(smartListItems -> m_api.createSmartListItems(smartList.getItemId(), smartListItems))
                .map(smartListItems -> smartListItems.size())
                .doOnCompleted(() -> Ln.v("Completed Pushed SmartList to server:" + smartList.getItemId()));
    }

    private Observable<Integer> pullAllDataFromServers(Date date){

        String afterDate = Constants.getDateFormat().format(date);

        // This gets the items from the server
        return getSyncPullSmartLists(afterDate)
                .flatMap(smartListItems -> Observable.from(smartListItems))
                .doOnNext(smartList -> Ln.v("Pulling list items for " + smartList.getName() + " list"))
                .flatMap(smartList -> getSyncSmartListItems(smartList, afterDate))
                .doOnCompleted(() -> Ln.v("Completed Get Items From Server"));

    }

    public void synchronizeSmartLists() {


        if(m_accountUtils.isLoggedIn()) {

            Ln.v("Synchronizing all smartlists");

            m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.PROGRESS));

            final Date after = new Date(m_prefs.getLong(Constants.PREFERENCE_LAST_SYNC_TIME_SMARTLISTS, 0));

            Observable.concat(
                    pushChangesToServer(after),
                    pullAllDataFromServers(new Date(0)))
                    //pullAllDataFromServers(after))
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<Integer>() {
                        @Override
                        public void onCompleted() {
                            m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.COMPLETED));
                            Ln.v("Synchronize completed");
                            m_prefs.edit().putLong(Constants.PREFERENCE_LAST_SYNC_TIME_SMARTLISTS, System.currentTimeMillis()).apply();
                        }

                        @Override
                        public void onError(Throwable e) {
                            m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE));
                            handleErrors(e);
                        }

                        @Override
                        public void onNext(Integer numItems) {
                            Ln.v("Synchronized " + numItems + " smartitems");
                        }
                    });
        }

    }


    private Observable<List<SmartList>> getSyncPullSmartLists(String afterDate) {
        return m_api.getListOfSmartLists(afterDate)
                .doOnNext(smartListItems -> Ln.v("Pulling " + smartListItems.size() + " smartlist(s)"))
                .flatMap(smartListItems -> m_smartListDAO.update(smartListItems));

    }


    private Observable<Integer> getSyncUpdateSmartList(final SmartList smartList, Date after) {
        return m_api.updateSmartList(smartList.getItemId(), smartList)
                // update the smartlist
                .flatMap(serverList -> m_smartListDAO.update(serverList))
                // update all the changed items
                .flatMap(count -> m_smartListItemDAO.getListOfChangedSmartListItems(smartList.getItemId(), after))
                .doOnNext(smartListItems -> Ln.v("Updating on server " + smartList.getItemId() + " list"))
                .flatMap(smartListItems -> m_api.createSmartListItems(smartList.getItemId(), smartListItems))
                .map(items -> appendSmartListAndCategory(items, smartList))
                .flatMap(smartListItems1 -> m_smartListItemDAO.update(smartListItems1));
    }


    private Observable<Integer> getSyncCreateSmartList(SmartList smartList) {


        return m_api.createSmartlist(smartList)
                .flatMap(serverList -> m_smartListDAO.updateAndAssignId(serverList))
                .map(itemId -> {
                    smartList.setItemId(itemId);
                    return itemId;
                })
                // Create all the items, if the smartlist is new all the items are too!
                .flatMap(itemId -> m_smartListItemDAO.getSmartListItems(itemId))
                .flatMap(smartListItemList -> m_api.createSmartListItems(smartList.getItemId(), smartListItemList))
                .doOnNext(smartListItems -> Ln.v("Creating on server " + smartList.getItemId() + " list"))
                .map(items -> appendSmartListAndCategory(items, smartList))
                .flatMap(serverSmartListItemList -> m_smartListItemDAO.update(serverSmartListItemList));
    }

    private Observable<Integer> getSyncSmartListItems(final SmartList smartList, String afterDate) {

        Ln.v("getSyncSmartListItems");

        return m_api.getSmartListItems(smartList.getItemId(), afterDate)
                .map(items -> appendSmartListAndCategory(items, smartList))
                //.flatMap(smartListItems -> m_smartListItemDAO.update(smartListItems));
                .flatMap(smartListItems -> m_smartListItemDAO.bulkReplaceAndInsert(smartListItems));
    }


    public void createSmartList(SmartList smartList) {
        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.PROGRESS));

        Ln.v("Sending SmartList to server:" + smartList.getName());

        // This also updates the itemId

        getSyncCreateSmartList(smartList)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.COMPLETED));
                    }

                    @Override
                    public void onError(Throwable e) {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.COMPLETED));
                        handleErrors(e);
                    }

                    @Override
                    public void onNext(Integer count) {
                        Ln.v("Created SmartList");
                        m_eventBus.post(new OnSmartListCreatedEvent());
                    }
                });

    }

    public void removeSharing(SmartList smartList) {
        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.PROGRESS));

        Ln.v("Removing from sharing:" + smartList.getName());

        m_smartListDAO.deleteByItemId(smartList.getItemId());

        m_api.leaveShare(smartList.getItemId())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE));
                    }

                    @Override
                    public void onError(Throwable e) {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE));
                        handleErrors(e);
                    }

                    @Override
                    public void onNext(ApiResponse count) {
                        Ln.v("Removed Share");
                    }
                });

    }


    public void deleteSmartList(SmartList smartList) {
        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.PROGRESS));

        Ln.v("Deleting SmartList from server:" + smartList.getName());

        smartList.setStatus(Status.ARCHIVED.toString());
        smartList.setLastModified(new Date());
        m_smartListDAO.update(smartList);

        m_api.updateSmartList(smartList.getItemId(), smartList)
                // update the smartlist
                .flatMap(serverList -> m_smartListDAO.update(serverList))
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE));
                    }

                    @Override
                    public void onError(Throwable e) {
                        m_eventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.IDLE));
                        handleErrors(e);
                    }

                    @Override
                    public void onNext(Integer count) {
                        Ln.v("Deleted SmartList");
                    }
                });

    }

    public Observable<ShareToken> generateShareToken(SmartList smartList){
            return m_api.generateShareToken(smartList.getItemId());
    }

    public Observable<ApiResponse> subscribeToShareToken(String token){
        return m_api.subscribeToShare(token);
    }

    public Observable<List<ShareGroup>> getListOfShares(long smartListId){
        return m_api.getListOfShares(smartListId)
                .flatMap(Observable::from)
                .filter(shareGroup -> !shareGroup.getRole().equalsIgnoreCase("owner"))
                .toList();
    }


    private List<SmartListItem> appendSmartListAndCategory(List<SmartListItem> items, SmartList smartList) {

        Ln.v("appendSmartListAndCategory:"+items.size());


        for (int i = 0; i < items.size(); i++) {
            SmartListItem item = items.get(i);
            item.setSmartListId(smartList.getItemId());

            MasterSmartListItem masterListItem = m_masterListItemDAO.findFirstByCriteria(null, "itemId=" + item.getMasterItemId(), null, null);

            if(masterListItem == null){
                Ln.v("Adding custom item to masterlist");
                m_masterListItemDAO.save(item.getMasterListItem());
                masterListItem = m_masterListItemDAO.findFirstByCriteria(null, "itemId=" + item.getMasterItemId(), null, null);
            }


            item.setCategoryColor(masterListItem.getCategoryColor());
            item.setCategoryName(masterListItem.getCategoryName());
            item.setCategoryId(masterListItem.getCategoryId());

            items.set(i, item);
        }
        return items;
    }


    @Override
    public void onStart() {
        m_eventBus.register(this);
    }


    @Override
    public void onStop() {
        m_eventBus.unregister(this);
    }

    @Subscribe
    public void onLogoutEvent(OnLogoutEvent event){
        m_smartListDAO.cleanTable(false);
        m_smartListItemDAO.cleanTable(false);
    }




}
