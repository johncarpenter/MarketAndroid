/*
 * Copyright (c) 2015. 2Lines Software,Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twolinessoftware.smarterlist.util;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * This message bus allows you to post a message from any thread and it will get handled and then
 * posted to the main thread for you.
 */
public class PostFromAnyThreadBus extends Bus
{
    public PostFromAnyThreadBus()
    {
        super(ThreadEnforcer.MAIN);
    }

    @Override
    public void post(final Object event)
    {
        if (Looper.myLooper() != Looper.getMainLooper())
        {
            // We're not in the main loop, so we need to get into it.
            (new Handler(Looper.getMainLooper())).post(new Runnable()
            {
                @Override
                public void run()
                {
                    // We're now in the main loop, we can post now
                    PostFromAnyThreadBus.super.post(event);
                }
            });
        }
        else
        {
            super.post(event);
        }
    }

    @Override
    public void unregister(final Object object)
    {
        //  Lots of edge cases with register/unregister that sometimes throw.
        try
        {
            super.unregister(object);
        }
        catch (IllegalArgumentException e)
        {
            // TODO: use Crashlytics unhandled exception logging
            Ln.e(e);
        }
    }
}
