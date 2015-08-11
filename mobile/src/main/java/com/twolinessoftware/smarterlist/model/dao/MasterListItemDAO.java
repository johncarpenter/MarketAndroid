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

import android.content.Context;

import com.twolinessoftware.android.orm.dto.DAO;
import com.twolinessoftware.android.orm.dto.DAOException;
import com.twolinessoftware.android.orm.provider.AbstractContentProvider;
import com.twolinessoftware.smarterlist.model.MasterSmartCategory;
import com.twolinessoftware.smarterlist.model.MasterSmartListItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import rx.Observable;

/**
 * Created by John on 2015-03-26.
 */
public class MasterListItemDAO extends DAO<MasterSmartListItem> {


    public MasterListItemDAO(Context context, AbstractContentProvider<MasterSmartListItem> provider) {
        super(context, provider);
    }

    public Observable<List<MasterSmartListItem>> monitorCategoryChanges(String listname){

        return monitoredQuery(null,"masterListName = ? and categoryId IS NOT NULL) GROUP BY (categoryId",new String[]{listname},"categoryName desc",false);
    }


    public Observable<List<MasterSmartListItem>> monitorPredictedMasterListItems(String masterListName){

        return monitoredQuery(null,"score > 0 and masterListName=?",new String[]{masterListName},"categoryId asc , name asc",false);

    }

    public Observable<List<MasterSmartListItem>> monitorMasterListItems(String masterListName){

        return monitoredQuery(null,"masterListName=?",new String[]{masterListName},"categoryId asc , name asc",false);

    }

    public Observable<List<MasterSmartListItem>> monitorMasterListItems(String masterListName, long categoryId){
        if (categoryId == 0) {
            return monitorMasterListItems(masterListName);
        }

        return monitoredQuery(null,"masterListName=? and categoryId = "+categoryId,new String[]{masterListName},"categoryId asc , name asc",false);

    }

    public Observable<List<MasterSmartListItem>> search(String masterListName, String queryText){

        return createFtSearch("masterListName = ? and name MATCH ?",new String[]{masterListName,queryText},"categoryId desc")
                .flatMap(query -> mapQuerytoList(query));


    }

    public Observable<List<MasterSmartListItem>> search(String masterListName,long categoryId, String queryText){

        if(categoryId == 0){
            return search(masterListName,queryText);
        }

        return createFtSearch("categoryId="+categoryId+" and masterListName = ? and name MATCH ?",new String[]{masterListName,queryText},"name asc")
                .flatMap(query -> mapQuerytoList(query));


    }

    public Observable<List<MasterSmartCategory>> queryCategories(String masterListName){
        return createQuery(null,"masterListName = ? and categoryId IS NOT NULL) GROUP BY (categoryId",new String[]{masterListName},"categoryName desc")
                .flatMap(query -> mapQuerytoList(query))
                .map(masterLists -> mapCategories(masterLists));
    }


    private List<MasterSmartCategory> mapCategories(List<MasterSmartListItem> items) {
        HashSet<MasterSmartCategory> categories = new HashSet<>();

        for(MasterSmartListItem item:items){
            MasterSmartCategory category = new MasterSmartCategory(item.getCategoryId(),item.getCategoryName(),item.getCategoryColor(),item.getCategoryIconUrl());
            categories.add(category);
        }

        return new ArrayList<>(categories);

    }


    public MasterSmartCategory findCategory(long categoryId) {
        MasterSmartListItem item = findFirstByCriteria(null,"categoryId = "+categoryId,null,null);
        return new MasterSmartCategory(item);
    }

    public Observable<List<MasterSmartListItem>> getListOfCustomItemsThatNeedSync(){

        return createQuery(null,"itemId=0",null,null)
                .flatMap(query->mapQuerytoList(query))
                .flatMap(masterListItems ->{
                    return Observable.create(subscriber -> {
                        subscriber.onNext(masterListItems);
                        subscriber.onCompleted();
                    });
                });


    }


    public MasterSmartListItem saveAndReturn(MasterSmartListItem masterSmartListItem) throws DAOException {
        int id = super.save(masterSmartListItem);
        return findById(id);
    }
}
