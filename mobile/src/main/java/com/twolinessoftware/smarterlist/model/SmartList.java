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

package com.twolinessoftware.smarterlist.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.twolinessoftware.android.orm.provider.annotation.Database;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.twolinessoftware.smarterlist.service.BaseCommunicationService;

import java.util.Date;

@Database(name="smartlist", version=1)
public class SmartList implements Parcelable{

    @DatabaseField(name="_id")
    @Index
    private int localId;

    public int getLocalId(){
        return localId;
    }

    @DatabaseField(name="itemId")
    @SerializedName("id")
    private long itemId;

    @DatabaseField(name="name")
    private String name;

    @DatabaseField(name="description")
    private String description;

    @DatabaseField(name="created")
    private Date created;

    @DatabaseField(name="masterListName")
    private String masterListName;

    @DatabaseField(name="iconUrl")
    private String iconUrl;

    @DatabaseField(name="lastModified")
    private Date lastModified;

    @DatabaseField(name="status")
    private String status;

    @DatabaseField(name="owner")
    private String owner;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SmartList( String name, String description, String masterListName, String iconUrl) {
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.masterListName = masterListName;
        this.created = new Date();
        this.lastModified = new Date();
        this.status = BaseCommunicationService.Status.ACTIVE.toString();
        this.itemId = 0;
    }

    public SmartList() {
        this.created = new Date();
        this.lastModified = new Date();
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(localId);
        dest.writeLong(itemId);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(created.getTime());
        dest.writeString(iconUrl);
        dest.writeString(masterListName);
        dest.writeString(status);
        dest.writeLong(lastModified.getTime());
    }

    private SmartList(Parcel in) {
        localId = in.readInt();
        itemId = in.readLong();
        name = in.readString();
        description = in.readString();
        created = new Date(in.readLong());
        iconUrl = in.readString();
        masterListName = in.readString();
        status = in.readString();
        lastModified = new Date(in.readLong());
    }

    public static final Parcelable.Creator<SmartList> CREATOR
            = new Parcelable.Creator<SmartList>() {
        public SmartList createFromParcel(Parcel in) {
            return new SmartList(in);
        }

        public SmartList[] newArray(int size) {
            return new SmartList[size];
        }
    };

    public String getMasterListName() {
        return masterListName;
    }

    public void setMasterListName(String masterListName) {
        this.masterListName = masterListName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public long getItemId() {

        long id = (itemId == 0)?localId:itemId;

        return id;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

}
