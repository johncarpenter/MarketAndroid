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

public class MasterSmartCategory implements Parcelable{

    private long id;

    private String name;

    private int color;

    private String iconUrl;

    public MasterSmartCategory(long id, String name, int color, String iconUrl) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.iconUrl = iconUrl;
    }

    public MasterSmartCategory(MasterSmartListItem item){
        this.id = item.getCategoryId();
        this.name = item.getCategoryName();
        this.color = item.getCategoryColor();
        this.iconUrl = item.getCategoryIconUrl();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MasterSmartCategory))
            return false;
        if (obj == this)
            return true;

        MasterSmartCategory rhs = (MasterSmartCategory) obj;
        return this.id == rhs.id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(color);
    }

    private MasterSmartCategory(Parcel in) {
        id = in.readLong();
        name = in.readString();
        color = in.readInt();
    }

    public static final Parcelable.Creator<MasterSmartCategory> CREATOR
            = new Parcelable.Creator<MasterSmartCategory>() {
        public MasterSmartCategory createFromParcel(Parcel in) {
            return new MasterSmartCategory(in);
        }

        public MasterSmartCategory[] newArray(int size) {
            return new MasterSmartCategory[size];
        }
    };

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
