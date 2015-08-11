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
import android.content.SharedPreferences;

import com.twolinessoftware.android.orm.dto.DAO;
import com.twolinessoftware.android.orm.provider.AbstractContentProvider;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.model.MasterSmartList;
import com.twolinessoftware.smarterlist.util.Ln;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by John on 2015-03-26.
 */
public class MasterSmartListDAO extends DAO<MasterSmartList> {


    @Inject
    SharedPreferences m_sharedPreferences;

    public MasterSmartListDAO(Context context, AbstractContentProvider<MasterSmartList> provider) {
        super(context, provider);
        Injector.inject(this);
    }

    public Observable<List<MasterSmartList>> monitorMasterSmartLists(){
        return monitoredQuery(null,null,null,"name asc",false);
    }

    public MasterSmartList findByName(String listname) {
        List<MasterSmartList> lists =  findByCriteria(null,"name=?",new String[]{listname},null);
        if (lists != null && !lists.isEmpty()) {
            return lists.get(0);
        }

        return null;

    }

    public Observable<List<MasterSmartList>> getListOfMasterListsForSyncing(boolean force){

        return createQuery(null,null,null,null)
                .flatMap(query->mapQuerytoList(query))
                .flatMap(smartLists ->{
                    return Observable.create(subscriber -> {
                        List<MasterSmartList> masterLists = new ArrayList<>();
                        for(MasterSmartList smartList:smartLists){
                            if(force || requiresUpdate(smartList)) {
                                masterLists.add(smartList);
                            }
                        }
                        subscriber.onNext(masterLists);
                        subscriber.onCompleted();
                    });
                });


    }




    private boolean requiresUpdate(MasterSmartList list){

        int currentVersion = m_sharedPreferences.getInt(Constants.PREFERENCE_CURRENT_LIST_VERSION + list.getName(), 0);
        int version = list.getVersion();

        Ln.v("Checking for updates of " + list.getName() + " version:" + currentVersion + " vs Server:" + version);

        return version > currentVersion;

    }
}
