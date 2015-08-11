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

package com.twolinessoftware.smarterlist.model.provider;

import android.net.Uri;

import com.twolinessoftware.android.orm.provider.MappedContentProvider;
import com.twolinessoftware.smarterlist.model.MasterSmartList;
import com.twolinessoftware.smarterlist.model.SmartList;

/**
 * Created by John on 2015-03-26.
 */
public class MasterSmartListProvider extends MappedContentProvider<MasterSmartList>{

        public static final String PROVIDER_NAME = "com.twolinessoftware.smarterlist.mastersmartlist";

        public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME);

        public static final String RETURN_TYPE ="vnd.android.cursor.dir/vnd.twolinessoftware.smarterlist.mastersmartlist";

        @Override
        public Uri getBaseContentUri() {
            return CONTENT_URI;
        }

        @Override
        public String getType(Uri uri) {
            return RETURN_TYPE;
        }

        @Override
        public String getProviderName() {
            return PROVIDER_NAME;
        }

}