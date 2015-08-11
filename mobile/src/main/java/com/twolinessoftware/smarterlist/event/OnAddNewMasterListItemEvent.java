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

package com.twolinessoftware.smarterlist.event;

/**
 * Created by John on 2015-04-09.
 */
public class OnAddNewMasterListItemEvent {
    private final String m_lastSearchQuery;
    private final String m_masterListName;

    public OnAddNewMasterListItemEvent(String lastSearchQuery, String masterListName) {
        this.m_lastSearchQuery = lastSearchQuery;
        this.m_masterListName = masterListName;
    }

    public String getLastSearchQuery() {
        return m_lastSearchQuery;
    }

    public String getMasterListName() {
        return m_masterListName;
    }
}
