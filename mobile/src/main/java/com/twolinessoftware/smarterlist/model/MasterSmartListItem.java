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

import com.twolinessoftware.android.orm.provider.annotation.Database;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;

@Database(name="mastersmartlistitem", version=1)
public class MasterSmartListItem {

    @DatabaseField(name="_id")
    @Index
    private int _id;

    @DatabaseField(name="masterListName")
    private String masterListName;

    @DatabaseField(name="itemId")
    private long id;

    @DatabaseField(name="categoryId")
    private long categoryId;

    @DatabaseField(name="categoryName")
    private String categoryName;

    @DatabaseField(name="categoryColor")
    private int categoryColor;

    @DatabaseField(name="categoryIconUrl")
    private String categoryIconUrl;

    @DatabaseField(name="name")
    private String name;

    @DatabaseField(name="description")
    private String description;

    @DatabaseField(name="custom")
    private boolean custom;

    @DatabaseField(name="score")
    private double score;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(int categoryColor) {
        this.categoryColor = categoryColor;
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


    public String getCategoryIconUrl() {
        return categoryIconUrl;
    }

    public void setCategoryIconUrl(String categoryIconUrl) {
        this.categoryIconUrl = categoryIconUrl;
    }

    public String getMasterListName() {
        return masterListName;
    }

    public void setMasterListName(String masterListName) {
        this.masterListName = masterListName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (int)this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MasterSmartListItem))
            return false;
        if (obj == this)
            return true;

        MasterSmartListItem rhs = (MasterSmartListItem) obj;
        return rhs.getId() == this.getId();// @todo add additional checks here for handling changed items
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
