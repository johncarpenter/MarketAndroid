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

public class ApiError {
	
	public static final ApiError API_ERROR_SERVICE_NOT_AVAILABLE = new ApiError(1,"Service is not available at this time");
	public static final ApiError API_ERROR_NOT_AUTHORIZED = new ApiError(2,"Not authorized for this method");
    public static final ApiError API_ERROR_PERMISSION = new ApiError(3,"You must be logged in to perform that action");

	public static final ApiError API_ERROR_MASTERLIST_NOT_FOUND = new ApiError(101,"No list by that name exists");
    public static final ApiError API_ERROR_CATEGORY_NOT_FOUND = new ApiError(102,"No list by that name exists");
    public static final ApiError API_ERROR_CONFLICT = new ApiError(103,"Object by that name exists");
    public static final ApiError API_ERROR_PASSWORD_TOO_SHORT = new ApiError(201,"Password must be at least 6 characters");
    public static final ApiError API_ERROR_EMAIL_TAKEN = new ApiError(202, "Email is taken");
    public static final ApiError API_ERROR_INVALID_EMAIL = new ApiError(203, "Email is invalid format");



    public ApiError(){}
	
	public ApiError(int code, String message) {
		super();
		this.code = code;
		this.message = message;
	}
	private int code; 
	private String message;

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	} 
	
}
