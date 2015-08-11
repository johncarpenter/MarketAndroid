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

import com.twolinessoftware.smarterlist.model.Token;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by John on 2015-04-02.
 */
public interface AccountApi {

    @FormUrlEncoded
    @POST("/user/register")
    Observable<Token> registerUser(@Field("e") String email, @Field("p") String password);

    @FormUrlEncoded
    @POST("/user/forgot")
    Observable<ApiResponse> resetPassword(@Field("e") String email);

    @FormUrlEncoded
    @POST("/user/gcm")
    Observable<ApiResponse> updateGcm(@Field("gcm") String gcm);

    @FormUrlEncoded
    @POST("/oauth2/token")
    Token getTokenSync(@Field("grant_type") String grantType, @Field("username") String username, @Field("password") String password,
                       @Field("client_id") String clientId, @Field("client_secret") String clientSecret, @Field("scope") String scope);

    @FormUrlEncoded
    @POST("/oauth2/token")
    Observable<Token> getToken(@Field("grant_type") String grantType, @Field("username") String username, @Field("password") String password,
                               @Field("client_id") String clientId, @Field("client_secret") String clientSecret, @Field("scope") String scope);

}
