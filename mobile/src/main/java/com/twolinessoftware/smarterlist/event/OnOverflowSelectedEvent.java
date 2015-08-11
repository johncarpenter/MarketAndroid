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

import android.view.View;

import com.twolinessoftware.smarterlist.model.SmartList;

/**
 * Created by John on 2015-03-31.
 */
public class OnOverflowSelectedEvent {
    private final SmartList m_smartItem;
    private final View m_anchorView;

    public OnOverflowSelectedEvent(View view, SmartList item) {
        this.m_smartItem = item;
        this.m_anchorView = view;
    }

    public SmartList getSmartList() {
        return m_smartItem;
    }

    public View getAnchorView() {
        return m_anchorView;
    }
}
