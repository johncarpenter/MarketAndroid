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

import com.twolinessoftware.smarterlist.model.ShareGroup;
import com.twolinessoftware.smarterlist.model.ShareToken;
import com.twolinessoftware.smarterlist.model.SmartList;
import com.twolinessoftware.smarterlist.model.SmartListItem;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by John on 2015-04-02.
 */
public interface SmartListApi {

    /**
     * Smartlist Section
     */
    @GET("/smartlist/list")
    Observable<List<SmartList>> getListOfSmartLists(@Query("after") String afterDate);

    @POST("/smartlist")
    Observable<SmartList> createSmartlist(@Body SmartList smartList);

    @GET("/smartlist/{smartlistId}")
    Observable<SmartList> getSmartList(@Path("smartlistId") long smartListId);

    @GET("/smartlist/{smartlistId}/item/list")
    Observable<List<SmartListItem>> getSmartListItems(@Path("smartlistId") long smartListId, @Query("after") String afterDate);

    @PUT("/smartlist/{smartlistId}")
    Observable<SmartList> updateSmartList(@Path("smartlistId") long smartListId, @Body SmartList smartList);

    @POST("/smartlist/{smartlistId}/items")
    Observable<List<SmartListItem>> createSmartListItems(@Path("smartlistId") long smartListId, @Body List<SmartListItem> smartListItem);


    @GET("/share/generate/{smartlistId}")
    Observable<ShareToken> generateShareToken(@Path("smartlistId") long smartListId);

    @GET("/share/subscribe/{token}")
    Observable<ApiResponse> subscribeToShare(@Path("token") String token);

    @POST("/share/{smartlistId}/leave")
    Observable<ApiResponse> leaveShare(@Path("smartlistId") long smartListId);

    @GET("/share/{smartlistId}/list")
    Observable<List<ShareGroup>> getListOfShares(@Path("smartlistId") long smartListId);


}
