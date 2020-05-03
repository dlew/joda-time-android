/*
 *  Copyright 2001-2014 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.danlew.android.joda.test;

import android.content.Context;


import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.danlew.android.joda.ResourceZoneInfoProvider;

import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

/**
 * Trying some stuff to debug what is going on...
 */
@RunWith(AndroidJUnit4.class)
public class SanityTest {

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        System.out.println("Context: " + context);
        DateTimeZone.setProvider(new ResourceZoneInfoProvider(context));
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getDefaultLocale() {
        System.out.println(Locale.getDefault());
    }

    @Test
    public void getDefaultDateTimeZone() {
        System.out.println(DateTimeZone.getDefault());
    }
}
