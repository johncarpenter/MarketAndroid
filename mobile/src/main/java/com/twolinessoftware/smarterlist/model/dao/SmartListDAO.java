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

package com.twolinessoftware.smarterlist.model.dao;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.twolinessoftware.android.orm.dto.DAO;
import com.twolinessoftware.android.orm.dto.DAOException;
import com.twolinessoftware.android.orm.provider.AbstractContentProvider;
import com.twolinessoftware.smarterlist.model.SmartList;
import com.twolinessoftware.smarterlist.service.BaseCommunicationService;
import com.twolinessoftware.smarterlist.util.Ln;

import java.util.Date;
import java.util.List;

import rx.Observable;

/**
 * Created by John on 2015-03-26.
 */
public class SmartListDAO extends DAO<SmartList> {


    public SmartListDAO(Context context, AbstractContentProvider<SmartList> provider) {
        super(context, provider);
    }

    public Observable<List<SmartList>> monitorSmartListChanges() {
        return createMonitoredQuery(null, "status = 'ACTIVE'", null, "created desc", false)
                .flatMap(query->mapQuerytoList(query));
    }

    /*@Override
    public boolean delete(long id) {
        ContentValues cv = new ContentValues();
        cv.put("status", BaseCommunicationService.Status.ARCHIVED.toString());
        cv.put("lastModified",new Date().getTime());
        int updatedCount = getContentResolver().update(getProvider().getBaseContentUri(),cv,"_id="+id,null);
        return updatedCount > 0;
    }*/

    public Observable<Integer> update(SmartList serverList) {
        return Observable.create(sub -> {
            sub.onNext(updateLocal(serverList));
            sub.onCompleted();
        });
    }

    public Observable<List<SmartList>> update(List<SmartList> smartLists) {

        return Observable.create(sub -> {

            int count = 0;
            for (SmartList item : smartLists) {

                count += updateLocal(item);
            }

            sub.onNext(smartLists);
            sub.onCompleted();

        });
    }


    public  Observable<Long> updateAndAssignId(SmartList serverList) {
        return Observable.create(sub -> {
            updateLocal(serverList);
            sub.onNext(serverList.getItemId());
            sub.onCompleted();
        });
    }


    @Override
    public int save(List<SmartList> bulk) throws DAOException {
        int count = 0;
        for(SmartList list:bulk){
            count += updateLocal(list);
        }
        return count;
    }

    @Override
    public int save(SmartList smartList) throws DAOException {
        return updateLocal(smartList);
    }

    private int updateLocal(SmartList serverList){

        ContentValues values = toContentValues(serverList);
        int count = 0;
        if(serverList.getItemId() != 0 && findByItemId(serverList.getItemId())!= null) {

            BaseCommunicationService.Status status = BaseCommunicationService.Status.valueOf(serverList.getStatus());
            if(status == BaseCommunicationService.Status.ARCHIVED){
                Ln.v("Archived, removing from local:"+serverList.getItemId());
                deleteByItemId(serverList.getItemId());
            }else{
                Ln.v("Updating itemId:" + serverList.getItemId());
                count = getContentResolver().update(getProvider().getBaseContentUri(), values, "itemId=" + serverList.getItemId(), null);
            }


        }else if( serverList.getLocalId() != 0 && findById(serverList.getLocalId()) != null ){

                getContentResolver().update(getProvider().getBaseContentUri(),values,"_id="+serverList.getLocalId(),null);

                Ln.v("Replacing localId:"+serverList.getLocalId());

                Uri contentUri = Uri.parse("content://com.twolinessoftware.smarterlist.smartlistitem");

                ContentValues cv = new ContentValues();
                cv.put("itemId",serverList.getItemId());
                return getContentResolver().update(contentUri, cv, "smartListId =" + serverList.getLocalId(), null);


        }else{
            Ln.v("Unable to find smartlist: adding new entry");
            count = super.save(serverList);
        }
        return count;
    }

    public void deleteByItemId(long itemId) {
        getContentResolver().delete(getProvider().getBaseContentUri(), "itemId=" + itemId, null);
        getContentResolver().notifyChange(getProvider().getBaseContentUri(),null);
    }


    public SmartList findByItemId(long itemId){
        return findFirstByCriteria(null,"itemId="+itemId,null,null);
    }

    public Observable<List<SmartList>> bulkServerUpdate(List<SmartList> smartLists) {
        return Observable.create(sub->{
            int updatedCount = 0;
            Ln.v("bulkServerUpdate Start:SmartLists");

            for(SmartList smartList:smartLists){
                SmartList local = null;

                updatedCount += updateLocal(smartList);
            }
            sub.onNext(smartLists);
            sub.onCompleted();
        });
    }


    public Observable<List<SmartList>> getListOfAllChangedSmartLists(final Date after) {
        return Observable.create(sub->{

            List<SmartList> smartLists =findByCriteria(null, "lastModified > " + after.getTime(), null, null);
            if(smartLists.size() == 0) {
                Ln.v("No smartlists have been modified locally");
            }
            sub.onNext(smartLists);
            sub.onCompleted();
        });
    }




    public Observable<List<SmartList>> getListOfNewSmartLists() {
        return Observable.create(sub -> {

            List<SmartList> smartLists = findByCriteria(null, "itemId=0", null, null);
            if (smartLists.size() == 0) {
                Ln.v("No new smartlists have been created");
                sub.onNext(smartLists);
                sub.onCompleted();
            } else {
                sub.onNext(smartLists);
            }

        });
    }

    public Observable<List<SmartList>> getListOfChangedSmartLists(final Date after) {
        return Observable.create(sub->{

            List<SmartList> smartLists =findByCriteria(null, "lastModified > " + after.getTime() + " and itemId != 0 ", null, null);
            if(smartLists.size() == 0) {
                Ln.v("No smartlists have been modified locally");
            }
            sub.onNext(smartLists);
            sub.onCompleted();
        });
    }

    public void updateLastModified(long smartListId, Date date) {

       SmartList smartList = findByItemId(smartListId);

        // not synced yet, use localId
        if(smartList == null){
            smartList = findById(smartListId);
        }

        if( smartList != null){
            smartList.setLastModified(date);
            super.save(smartList);
        }else{
            Ln.e("Unable to updateLastModified timestamp, invalid id:"+smartListId);
        }

    }

}
