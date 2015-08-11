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

import com.google.gson.annotations.SerializedName;
import com.twolinessoftware.android.orm.provider.annotation.Database;
import com.twolinessoftware.android.orm.provider.annotation.DatabaseField;
import com.twolinessoftware.android.orm.provider.annotation.Index;
import com.twolinessoftware.smarterlist.service.BaseCommunicationService;

import java.util.Date;

@Database(name="smartlistitem", version=1)
public class SmartListItem {

    @DatabaseField(name="_id")
    @Index
    private int localId;

    @DatabaseField(name="itemId")
    @SerializedName("id")
    private long itemId;

    @DatabaseField(name="smartListId")
    private long smartListId;

    @DatabaseField(name="notes")
    private String notes;

    @DatabaseField(name="quantity")
    private int quantity;

    @DatabaseField(name="masterItemId")
    private long masterItemId;

    @DatabaseField(name="categoryId")
    private transient long categoryId;

    @DatabaseField(name="categoryName")
    private transient String categoryName;

    @DatabaseField(name="categoryColor")
    private transient int categoryColor;

    @DatabaseField(name="name")
    private String name;

    @DatabaseField(name="description")
    private String description;

    @DatabaseField(name="checked")
    private boolean checked;

    @DatabaseField(name="lastModified")
    private Date lastModified;

    @DatabaseField(name="status")
    private String status;

    private MasterSmartListItem masterListItem;


    public SmartListItem(MasterSmartListItem item, String notes, int quantity, long smartListId) {
        this.notes = notes;
        this.quantity = quantity;
        this.smartListId = smartListId;
        this.masterItemId = item.getId();
        this.categoryId = item.getCategoryId();
        this.categoryName = item.getCategoryName();
        this.categoryColor = item.getCategoryColor();
        this.name = item.getName();
        this.description = item.getDescription();
        this.checked = false;
        this.lastModified = new Date();
        this.status = BaseCommunicationService.Status.ACTIVE.toString();
    }

    public SmartListItem() {
    }

    public long getSmartListId() {
        return smartListId;
    }

    public void setSmartListId(long smartListId) {
        this.smartListId = smartListId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getMasterItemId() {
        return masterItemId;
    }

    public void setMasterItemId(long masterItemId) {
        this.masterItemId = masterItemId;
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

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
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

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public int getLocalId() {
        return localId;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (int)this.itemId + this.localId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SmartListItem))
            return false;
        if (obj == this)
            return true;

        SmartListItem rhs = (SmartListItem) obj;

        if(rhs.getItemId() == 0 || getItemId() == 0){
            return rhs.getLocalId() == this.getLocalId();
        }else{
            return rhs.getItemId() == this.getItemId();// @todo add additional checks here for handling changed items
        }



    }

    public MasterSmartListItem getMasterListItem() {
        return masterListItem;
    }

    public void setMasterListItem(MasterSmartListItem masterListItem) {
        this.masterListItem = masterListItem;
    }
}
