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

import com.twolinessoftware.smarterlist.fragment.BaseFragment;

/**
 * Created by John on 2015-03-29.
 */
public class OnNavigateToFragment {
    private final BaseFragment m_baseFragment;

    public OnNavigateToFragment(BaseFragment baseFragment) {
        this.m_baseFragment = baseFragment;
    }

    public BaseFragment getBaseFragment() {
        return m_baseFragment;
    }
}
