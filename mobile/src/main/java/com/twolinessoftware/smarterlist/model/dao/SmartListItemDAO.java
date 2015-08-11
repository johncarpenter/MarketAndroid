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
import android.database.Cursor;

import com.twolinessoftware.android.orm.dto.DAO;
import com.twolinessoftware.android.orm.dto.DAOException;
import com.twolinessoftware.android.orm.provider.AbstractContentProvider;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.model.SmartListItem;
import com.twolinessoftware.smarterlist.service.BaseCommunicationService;
import com.twolinessoftware.smarterlist.util.AccountUtils;
import com.twolinessoftware.smarterlist.util.Ln;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.schedulers.Schedulers;

public class SmartListItemDAO extends DAO<SmartListItem> {

    @Inject
    SmartListDAO m_smartListDAO;

    @Inject
    AccountUtils m_accountUtils;


    public SmartListItemDAO(Context context, AbstractContentProvider<SmartListItem> provider) {
        super(context, provider);
        Injector.inject(this);
    }

    public Observable<List<SmartListItem>> monitorSmartList(long smartListId) {
        return createMonitoredQuery(null, "status = 'ACTIVE' and smartListId = " + smartListId, null, "checked asc, categoryId desc", false)
                .flatMap(query -> mapQuerytoList(query));
    }

    public Observable<List<SmartListItem>> monitorSmartListUnchecked(long smartListId) {
        return monitoredQuery(null, "status = 'ACTIVE' and checked = 0 and smartListId = " + smartListId, null, "categoryId desc", false);
    }

    public Observable<List<SmartListItem>> getSmartListItems(long smartListId) {
        return createQuery(null, "status = 'ACTIVE' and smartListId = " + smartListId, null, "checked asc, categoryId desc")
                .flatMap(query -> mapQuerytoList(query));
    }

    public void asyncSetChecked(List<SmartListItem> items){

        Observable.from(items)
                .map(item -> {item.setChecked(true); return item;})
                .map(item -> save(item))
                .subscribeOn(Schedulers.newThread())
                .subscribe(count->{});

    }




    public Observable<HashMap<Long, Integer>> monitorSmartListUncheckedCounts() {
        return createMonitoredQuery(new String[]{"smartListId", "count(*)"}, "status = 'ACTIVE' and checked = 0) group by (smartListId", null, null, false)
                .flatMap(query -> {
                    Cursor cursor = query.run();
                    HashMap<Long, Integer> map = new HashMap<>();
                    while (cursor.moveToNext()) {
                        long smartListId = cursor.getLong(0);
                        int count = cursor.getInt(1);
                        Ln.v("Unchecked Count:"+smartListId+" is "+count+" items");
                        map.put(smartListId,count);

                    }
                    return Observable.just(map);
                });
    }

    @Override
    public int save(SmartListItem smartListItem) throws DAOException {

        // update the lastmodified in the smartlist
        m_smartListDAO.updateLastModified(smartListItem.getSmartListId(), new Date());


        return update(smartListItem);
    }

    @Override
    public int save(List<SmartListItem> bulk) throws DAOException {

        if (bulk.size() > 0) {
            SmartListItem firstItem = bulk.get(0);
            // update the lastmodified in the smartlist
            m_smartListDAO.updateLastModified(firstItem.getSmartListId(), new Date());
        }

        int count = 0;
        for (SmartListItem smartListItem : bulk) {
            update(smartListItem);
        }

        return count;
    }

    public Observable<Integer> bulkServerUpdate(List<SmartListItem> smartLists) {
        return Observable.create(sub -> {
            int updatedCount = 0;
            Ln.v("bulkServerUpdate Start:SmartListItems");

            for (SmartListItem smartListItem : smartLists) {
                updatedCount += save(smartListItem);
            }
            sub.onNext(updatedCount);
            sub.onCompleted();
        });
    }


    public Observable<List<SmartListItem>> getListOfChangedSmartListItems(long smartListId, Date after) {

        return Observable.create(sub -> {

            List<SmartListItem> smartLists = findByCriteria(null, "smartListId = " + smartListId + " and lastModified >" + after.getTime()+"  and masterItemId > 0", null, null);
            if (smartLists.size() == 0) {
                Ln.v("No smartlistitems have been modified locally for list " + smartListId);
            }else {
                sub.onNext(smartLists);
            }
            sub.onCompleted();
        });


    }

    public Observable<Integer> update(List<SmartListItem> smartListItems) {

        return Observable.create(sub -> {

            int count = 0;
            for (SmartListItem item : smartListItems) {

                count += update(item);
            }

            sub.onNext(count);
            sub.onCompleted();

        });
    }

    public int update(SmartListItem item) {

        int count = 0;
        ContentValues values = toContentValues(item);

        if (item.getItemId() != 0 && findByItemId(item.getItemId()) != null) {

            BaseCommunicationService.Status status = BaseCommunicationService.Status.valueOf(item.getStatus());
            if(status == BaseCommunicationService.Status.ARCHIVED){
                Ln.v("Archived, removing from local:"+item.getItemId());
                getContentResolver().delete(getProvider().getBaseContentUri(), "itemId=" + item.getItemId(), null);
            }else{
                Ln.v("Updating itemId:" + item.getItemId());
                count += getContentResolver().update(getProvider().getBaseContentUri(), values, "itemId=" + item.getItemId(), null);
            }

        } else if (item.getLocalId() != 0 && findById(item.getLocalId()) != null) {

            count += getContentResolver().update(getProvider().getBaseContentUri(), values, "_id=" + item.getLocalId(), null);

            Ln.v("Updating localId:" + item.getLocalId());

        } else {
            Ln.v("Unable to find smartlistitem: adding new entry");
            super.save(item);
            count++;
        }
        return count;
    }


    public SmartListItem findByItemId(long itemId) {
        return findFirstByCriteria(null, "itemId=" + itemId, null, null);
    }


    public void toggleIncluded(SmartListItem item) {
        if(hasItemInSmartList(item.getMasterItemId(), item.getSmartListId())){
            removeItem(item.getSmartListId(),item.getItemId());
        }else{
            SmartListItem checkItem = findFirstByCriteria(null, "masterItemId=" + item.getMasterItemId() + " and smartListId = "+item.getSmartListId(), null, null);
            if(checkItem != null){
                item.setItemId(checkItem.getItemId());
            }
            save(item);
        }
    }

    public void toggleChecked(SmartListItem item) {
        item.setChecked(!item.isChecked());
        item.setLastModified(new Date());
        save(item);
    }

    private boolean hasItemInSmartList(long masterItemId, long smartListId){
        return findFirstByCriteria(null, "masterItemId=" + masterItemId + " and smartListId = "+smartListId, null, null) != null;
    }

    private boolean removeItem(long smartListId, long itemId) {
        String where = "smartListId=" + smartListId + " and masterItemId=" + itemId;

        ContentValues values = new ContentValues();
        values.put("lastModified", new Date().getTime());
        values.put("status", "ARCHIVED");

        int count = getContentResolver().update(getProvider().getBaseContentUri(), values, where, null);

        // update the lastmodified in the smartlist
        if(count != 0) {
            m_smartListDAO.updateLastModified(smartListId, new Date());
        }

        return count != 0;
    }
}
