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

import com.twolinessoftware.smarterlist.model.MasterSmartCategory;
import com.twolinessoftware.smarterlist.model.MasterSmartList;
import com.twolinessoftware.smarterlist.model.MasterSmartListItem;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import rx.Observable;

public interface MasterListApi {

    @GET("/masterlist/{name}")
    Observable<MasterSmartList> getMasterList(@Path("name") String collectionName);

    @GET("/masterlist/{name}/version")
    Observable<VersionWrapper> getMasterListVersion(@Path("name") String collectionName);


    @GET("/masterlist/{name}/item/list")
    Observable<List<MasterSmartListItem>> getItemsInList(@Path("name") String collectionName);


    @GET("/masterlist/{name}/category/list")
    Observable<List<MasterSmartCategory>> getAllCategories(@Path("name") String collectionName);


    @GET("/masterlist/list")
    Observable<List<MasterSmartList>> getMasterLists();

    @POST("/masterlist/custom/{name}/item")
    Observable<MasterSmartListItem> addNewMasterlistItem(@Path("name") String listname, @Body MasterListService.MasterListItemPostWrapper item);


}
