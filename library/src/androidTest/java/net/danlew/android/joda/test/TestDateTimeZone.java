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
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.JodaTimePermission;
import org.joda.time.LocalDateTime;
import org.joda.time.tz.DefaultNameProvider;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilePermission;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.text.DateFormatSymbols;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This is a version of TestDateTimeZone (from joda-time) that uses
 * ResourceZoneInfoProvider instead of ZoneInfoProvider.
 *
 * There are some subtle differences between the original TestDateTimeZone
 * and this one due to the need to initialize the ResourceZoneInfoProvider;
 * namely some variables are lazily initialized.
 *
 * Also, a lot of the "name" tests have been removed because they aren't the
 * same between the normal JDK and Android (as the Locales are implemented
 * slightly differently; e.g., different names).
 */
@RunWith(AndroidJUnit4.class)
public class TestDateTimeZone {
    private static final boolean OLD_JDK;
    static {
        String str = System.getProperty("java.version");
        boolean old = true;
        if (str.length() > 3 &&
            str.charAt(0) == '1' &&
            str.charAt(1) == '.' &&
            (str.charAt(2) == '4' || str.charAt(2) == '5' || str.charAt(2) == '6')) {
            old = false;
        }
        OLD_JDK = old;
    }
    
    // Test in 2002/03 as time zones are more well known
    // (before the late 90's they were all over the place)

    // These have to be initialized *after* we init; don't want to shift code
    // too much so we keep the same naming convention, even though they are
    // no longer final
    private static DateTimeZone PARIS;
    private static DateTimeZone LONDON;
    
    long y2002days = 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 
                     366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 
                     365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 +
                     366 + 365;
    long y2003days = 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 
                     366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 
                     365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 +
                     366 + 365 + 365;
    
    // 2002-06-09
    private long TEST_TIME_SUMMER =
            (y2002days + 31L + 28L + 31L + 30L + 31L + 9L -1L) * DateTimeConstants.MILLIS_PER_DAY;
            
    // 2002-01-09
    private long TEST_TIME_WINTER =
            (y2002days + 9L -1L) * DateTimeConstants.MILLIS_PER_DAY;
            
//    // 2002-04-05 Fri
//    private long TEST_TIME1 =
//            (y2002days + 31L + 28L + 31L + 5L -1L) * DateTimeConstants.MILLIS_PER_DAY
//            + 12L * DateTimeConstants.MILLIS_PER_HOUR
//            + 24L * DateTimeConstants.MILLIS_PER_MINUTE;
//        
//    // 2003-05-06 Tue
//    private long TEST_TIME2 =
//            (y2003days + 31L + 28L + 31L + 30L + 6L -1L) * DateTimeConstants.MILLIS_PER_DAY
//            + 14L * DateTimeConstants.MILLIS_PER_HOUR
//            + 28L * DateTimeConstants.MILLIS_PER_MINUTE;
    
    private static final Policy RESTRICT;
    private static final Policy ALLOW;
    static {
        // don't call Policy.getPolicy()
        RESTRICT = new Policy() {
            public PermissionCollection getPermissions(CodeSource codesource) {
                Permissions p = new Permissions();
                p.add(new AllPermission());  // enable everything
                return p;
            }
            public void refresh() {
            }
            public boolean implies(ProtectionDomain domain, Permission permission) {
                if (permission instanceof JodaTimePermission) {
                    return false;
                }
                return true;
//                return super.implies(domain, permission);
            }
        };
        ALLOW = new Policy() {
            public PermissionCollection getPermissions(CodeSource codesource) {
                Permissions p = new Permissions();
                p.add(new AllPermission());  // enable everything
                return p;
            }
            public void refresh() {
            }
        };
    }
    
    private DateTimeZone zone;
    private Locale locale;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getContext();
        DateTimeZone.setProvider(new ResourceZoneInfoProvider(context));

        // Need to initialize these after ResourceZoneInfoProvider.init()
        PARIS = DateTimeZone.forID("Europe/Paris");
        LONDON = DateTimeZone.forID("Europe/London");

        locale = Locale.getDefault();
        zone = DateTimeZone.getDefault();
        Locale.setDefault(Locale.UK);
    }

    @After
    public void tearDown() throws Exception {
        // Weird emulators can fail to get default locales/zones, don't try setting these if null
        if (locale != null) {
            Locale.setDefault(locale);
        }
        if (zone != null) {
            DateTimeZone.setDefault(zone);
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testDefault() {
        assertNotNull(DateTimeZone.getDefault());
        
        DateTimeZone.setDefault(PARIS);
        assertSame(PARIS, DateTimeZone.getDefault());
        
        try {
            DateTimeZone.setDefault(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testDefaultSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            DateTimeZone.setDefault(PARIS);
            fail();
        } catch (SecurityException ex) {
            // ok
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testForID_String() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID((String) null));
        
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals("Europe/London", zone.getID());
        
        zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forID("+00");
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forID("+01:23");
        assertEquals("+01:23", zone.getID());
        assertEquals(DateTimeConstants.MILLIS_PER_HOUR + (23L * DateTimeConstants.MILLIS_PER_MINUTE),
                zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forID("-02:00");
        assertEquals("-02:00", zone.getID());
        assertEquals((-2L * DateTimeConstants.MILLIS_PER_HOUR),
                zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forID("-07:05:34.0");
        assertEquals("-07:05:34", zone.getID());
        assertEquals((-7L * DateTimeConstants.MILLIS_PER_HOUR) +
                    (-5L * DateTimeConstants.MILLIS_PER_MINUTE) +
                    (-34L * DateTimeConstants.MILLIS_PER_SECOND),
                    zone.getOffset(TEST_TIME_SUMMER));
        
        try {
            DateTimeZone.forID("SST");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("europe/london");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("Europe/UK");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("+");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forID("+0");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    // This entire test is invalid for the Android version
    /*
    public void testForID_String_old() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("GMT", "UTC");
        map.put("WET", "WET");
        map.put("CET", "CET");
        map.put("MET", "CET");
        map.put("ECT", "CET");
        map.put("EET", "EET");
        map.put("MIT", "Pacific/Apia");
        map.put("HST", "Pacific/Honolulu");
        map.put("AST", "America/Anchorage");
        map.put("PST", "America/Los_Angeles");
        map.put("MST", "America/Denver");
        map.put("PNT", "America/Phoenix");
        map.put("CST", "America/Chicago");
        map.put("EST", "America/New_York");
        map.put("IET", "America/Indiana/Indianapolis");
        map.put("PRT", "America/Puerto_Rico");
        map.put("CNT", "America/St_Johns");
        map.put("AGT", "America/Argentina/Buenos_Aires");
        map.put("BET", "America/Sao_Paulo");
        map.put("ART", "Africa/Cairo");
        map.put("CAT", "Africa/Harare");
        map.put("EAT", "Africa/Addis_Ababa");
        map.put("NET", "Asia/Yerevan");
        map.put("PLT", "Asia/Karachi");
        map.put("IST", "Asia/Kolkata");
        map.put("BST", "Asia/Dhaka");
        map.put("VST", "Asia/Ho_Chi_Minh");
        map.put("CTT", "Asia/Shanghai");
        map.put("JST", "Asia/Tokyo");
        map.put("ACT", "Australia/Darwin");
        map.put("AET", "Australia/Sydney");
        map.put("SST", "Pacific/Guadalcanal");
        map.put("NST", "Pacific/Auckland");
        for (String key : map.keySet()) {
            String value = map.get(key);
            TimeZone juZone = TimeZone.getTimeZone(key);
            DateTimeZone zone = DateTimeZone.forTimeZone(juZone);
            assertEquals(DateTimeZone.forID(value), zone);
//            System.out.println(juZone);
//            System.out.println(juZone.getDisplayName());
//            System.out.println(zone);
//            System.out.println("------");
        }
        // gee thanks time-zone db maintainer for damaging the database
        // and breaking the long-standing  association with CAT/EAT
        TimeZone juZone = TimeZone.getTimeZone("CAT");
        DateTimeZone zone = DateTimeZone.forTimeZone(juZone);
        assertTrue(zone.getID().equals("Africa/Harare") || zone.getID().equals("Africa/Maputo"));
    }
    */

    //-----------------------------------------------------------------------
    @Test
    public void testForOffsetHours_int() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        assertEquals(DateTimeZone.forID("+03:00"), DateTimeZone.forOffsetHours(3));
        assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetHours(-2));
        try {
            DateTimeZone.forOffsetHours(999999);
            fail();
        } catch (IllegalArgumentException ex) {}
    }        

    //-----------------------------------------------------------------------
    @Test
    public void testForOffsetHoursMinutes_int_int() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
        assertEquals(DateTimeZone.forID("+23:59"), DateTimeZone.forOffsetHoursMinutes(23, 59));
        
        assertEquals(DateTimeZone.forID("+02:15"), DateTimeZone.forOffsetHoursMinutes(2, 15));
        assertEquals(DateTimeZone.forID("+02:00"), DateTimeZone.forOffsetHoursMinutes(2, 0));
        try {
            DateTimeZone.forOffsetHoursMinutes(2, -15);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(DateTimeZone.forID("+00:15"), DateTimeZone.forOffsetHoursMinutes(0, 15));
        assertEquals(DateTimeZone.forID("+00:00"), DateTimeZone.forOffsetHoursMinutes(0, 0));
        assertEquals(DateTimeZone.forID("-00:15"), DateTimeZone.forOffsetHoursMinutes(0, -15));
        
        assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetHoursMinutes(-2, 0));
        assertEquals(DateTimeZone.forID("-02:15"), DateTimeZone.forOffsetHoursMinutes(-2, -15));
        assertEquals(DateTimeZone.forID("-02:15"), DateTimeZone.forOffsetHoursMinutes(-2, 15));
        
        assertEquals(DateTimeZone.forID("-23:59"), DateTimeZone.forOffsetHoursMinutes(-23, 59));
        try {
            DateTimeZone.forOffsetHoursMinutes(2, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(-2, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(24, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(-24, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }        

    //-----------------------------------------------------------------------
    @Test
    public void testForOffsetMillis_int() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
        assertEquals(DateTimeZone.forID("+23:59:59.999"), DateTimeZone.forOffsetMillis((24 * 60 * 60 * 1000) - 1));
        assertEquals(DateTimeZone.forID("+03:00"), DateTimeZone.forOffsetMillis(3 * 60 * 60 * 1000));
        assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetMillis(-2 * 60 * 60 * 1000));
        assertEquals(DateTimeZone.forID("-23:59:59.999"), DateTimeZone.forOffsetMillis((-24 * 60 * 60 * 1000) + 1));
        assertEquals(DateTimeZone.forID("+04:45:17.045"),
                DateTimeZone.forOffsetMillis(
                        4 * 60 * 60 * 1000 + 45 * 60 * 1000 + 17 * 1000 + 45));
    }        

    //-----------------------------------------------------------------------
    @Test
    public void testForTimeZone_TimeZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone((TimeZone) null));
        
        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London"));
        assertEquals("Europe/London", zone.getID());
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("+00:00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00"));
        assertSame(DateTimeZone.UTC, zone);
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+01:23"));
        assertEquals("+01:23", zone.getID());
        assertEquals(DateTimeConstants.MILLIS_PER_HOUR + (23L * DateTimeConstants.MILLIS_PER_MINUTE),
                zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+1:23"));
        assertEquals("+01:23", zone.getID());
        assertEquals(DateTimeConstants.MILLIS_PER_HOUR + (23L * DateTimeConstants.MILLIS_PER_MINUTE),
                zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-02:00"));
        assertEquals("-02:00", zone.getID());
        assertEquals((-2L * DateTimeConstants.MILLIS_PER_HOUR), zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+2"));
        assertEquals("+02:00", zone.getID());
        assertEquals((2L * DateTimeConstants.MILLIS_PER_HOUR), zone.getOffset(TEST_TIME_SUMMER));
        
        zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", zone.getID());

        TimeZone tz = TimeZone.getTimeZone("GMT-08:00");
        tz.setID("GMT-\u0660\u0668:\u0660\u0660");
        zone = DateTimeZone.forTimeZone(tz);
        assertEquals("-08:00", zone.getID());
    }

    @Test
    public void testFromTimeZoneInvalid() throws Exception {
        TimeZone jdkZone = new TimeZone() {
            private static final long serialVersionUID = 1L;
            @Override
            public String getID() {
                return null;
            }
            @Override
            public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
                return 0;
            }
            @Override
            public void setRawOffset(int offsetMillis) {
            }
            @Override
            public int getRawOffset() {
                return 0;
            }
            @Override
            public boolean useDaylightTime() {
                return false;
            }
            @Override
            public boolean inDaylightTime(Date date) {
                return false;
            }
        };
        try {
            DateTimeZone.forTimeZone(jdkZone);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testTimeZoneConversion() {
        TimeZone jdkTimeZone = TimeZone.getTimeZone("GMT-10");
        assertEquals("GMT-10:00", jdkTimeZone.getID());
        
        DateTimeZone jodaTimeZone = DateTimeZone.forTimeZone(jdkTimeZone);
        assertEquals("-10:00", jodaTimeZone.getID());
        assertEquals(jdkTimeZone.getRawOffset(), jodaTimeZone.getOffset(0L));
        
        TimeZone convertedTimeZone = jodaTimeZone.toTimeZone();
        assertEquals("GMT-10:00", jdkTimeZone.getID());
        
        assertEquals(jdkTimeZone.getID(), convertedTimeZone.getID());
        assertEquals(jdkTimeZone.getRawOffset(), convertedTimeZone.getRawOffset());
    }

    //-----------------------------------------------------------------------
    @Test
    public void testGetAvailableIDs() {
        assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testProvider_badClassName() {
        try {
            System.setProperty("org.joda.time.DateTimeZone.Provider", "xxx");
            DateTimeZone.setProvider(null);

        } catch (RuntimeException ex) {
            // expected
            assertEquals(ResourceZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
        } finally {
            System.getProperties().remove("org.joda.time.DateTimeZone.Provider");
            DateTimeZone.setProvider(null);
        }
    }

    @Test
    public void testProviderSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            DateTimeZone.setProvider(new MockOKProvider());
            fail();
        } catch (SecurityException ex) {
            // ok
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
    }

    // We don't use the same resource loading as the base lib!
    /*
     public void testZoneInfoProviderResourceLoading() {
         final Set<String> ids = new HashSet<String>(DateTimeZone.getAvailableIDs());
         ids.remove(DateTimeZone.getDefault().getID());
        final String id = ids.toArray(new String[ids.size()])[new Random().nextInt(ids.size())];
        try {
            Policy.setPolicy(new Policy() {
                @Override
                public PermissionCollection getPermissions(CodeSource codesource) {
                    Permissions p = new Permissions();
                    p.add(new AllPermission());  // enable everything
                    return p;
                }
                @Override
                public void refresh() {
                }
                @Override
                public boolean implies(ProtectionDomain domain, Permission permission) {
                    return !(permission instanceof FilePermission) && !permission.getName().contains(id);
                 }
             });
             System.setSecurityManager(new SecurityManager());
             // will throw IllegalArgumentException if the resource can
             // not be loaded
             final DateTimeZone zone = DateTimeZone.forID(id);
             assertNotNull(zone);
         } finally {
             System.setSecurityManager(null);
             Policy.setPolicy(ALLOW);
         }
     }
    */

    static class MockNullIDSProvider implements Provider {
        public Set getAvailableIDs() {
            return null;
        }
        public DateTimeZone getZone(String id) {
            return null;
        }
    }
    static class MockEmptyIDSProvider implements Provider {
        public Set getAvailableIDs() {
            return new HashSet();
        }
        public DateTimeZone getZone(String id) {
            return null;
        }
    }
    static class MockNoUTCProvider implements Provider {
        public Set getAvailableIDs() {
            Set set = new HashSet();
            set.add("Europe/London");
            return set;
        }
        public DateTimeZone getZone(String id) {
            return null;
        }
    }
    static class MockBadUTCProvider implements Provider {
        public Set getAvailableIDs() {
            Set set = new HashSet();
            set.add("UTC");
            set.add("Europe/London");
            return set;
        }
        public DateTimeZone getZone(String id) {
            return null;
        }
    }
    static class MockOKProvider implements Provider {
        public Set getAvailableIDs() {
            Set set = new HashSet();
            set.add("UTC");
            set.add("Europe/London");
            return set;
        }
        public DateTimeZone getZone(String id) {
            return DateTimeZone.UTC;
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testNameProvider() {
        try {
            assertNotNull(DateTimeZone.getNameProvider());
        
            NameProvider provider = DateTimeZone.getNameProvider();
            DateTimeZone.setNameProvider(null);
            assertEquals(provider.getClass(), DateTimeZone.getNameProvider().getClass());
        
            provider = new MockOKButNullNameProvider();
            DateTimeZone.setNameProvider(provider);
            assertSame(provider, DateTimeZone.getNameProvider());
            
            assertEquals("+00:00", DateTimeZone.UTC.getShortName(TEST_TIME_SUMMER));
            assertEquals("+00:00", DateTimeZone.UTC.getName(TEST_TIME_SUMMER));
        } finally {
            DateTimeZone.setNameProvider(null);
        }
        
        try {
            System.setProperty("org.joda.time.DateTimeZone.NameProvider", "org.joda.time.tz.DefaultNameProvider");
            DateTimeZone.setNameProvider(null);
            assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
        } finally {
            System.getProperties().remove("org.joda.time.DateTimeZone.NameProvider");
            DateTimeZone.setNameProvider(null);
            assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
        }
    }

    @Test
    public void testNameProvider_badClassName() {
        try {
            System.setProperty("org.joda.time.DateTimeZone.NameProvider", "xxx");
            DateTimeZone.setProvider(null);

        } catch (RuntimeException ex) {
            // expected
            assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
        } finally {
            System.getProperties().remove("org.joda.time.DateTimeZone.NameProvider");
            DateTimeZone.setProvider(null);
        }
    }

    @Test
    public void testNameProviderSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            DateTimeZone.setNameProvider(new MockOKButNullNameProvider());
            fail();
        } catch (SecurityException ex) {
            // ok
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
    }

    static class MockOKButNullNameProvider implements NameProvider {
        public String getShortName(Locale locale, String id, String nameKey) {
            return null;
        }
        public String getName(Locale locale, String id, String nameKey) {
            return null;
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testConstructor() {
        assertEquals(1, DateTimeZone.class.getDeclaredConstructors().length);
        assertTrue(Modifier.isProtected(DateTimeZone.class.getDeclaredConstructors()[0].getModifiers()));
        try {
            new DateTimeZone(null) {
                public String getNameKey(long instant) {
                    return null;
                }
                public int getOffset(long instant) {
                    return 0;
                }
                public int getStandardOffset(long instant) {
                    return 0;
                }
                public boolean isFixed() {
                    return false;
                }
                public long nextTransition(long instant) {
                    return 0;
                }
                public long previousTransition(long instant) {
                    return 0;
                }
                public boolean equals(Object object) {
                    return false;
                }
            };
        } catch (IllegalArgumentException ex) {}
    }

    //-----------------------------------------------------------------------
    @Test
    public void testGetID() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals("Europe/Paris", zone.getID());
    }

    @Test
    public void testGetNameKey() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals("BST", zone.getNameKey(TEST_TIME_SUMMER));
        assertEquals("GMT", zone.getNameKey(TEST_TIME_WINTER));
    }

    static final boolean JDK6PLUS;
    static {
        boolean jdk6 = true;
        try {
            DateFormatSymbols.class.getMethod("getInstance", new Class[] { Locale.class });
        } catch (Exception ex) {
            jdk6 = false;
        }
        JDK6PLUS = jdk6;
    }

    static final boolean JDK9;
    static {
        boolean jdk9 = true;
        try {
            String str = System.getProperty("java.version");
            jdk9 = str.startsWith("9");
        } catch (Exception ex) {
            jdk9 = false;
        }
        JDK9 = jdk9;
    }

    // Names are not the same between JDK and Android
    /*
    public void testGetShortName() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals("BST", zone.getShortName(TEST_TIME_SUMMER));
        assertEquals("GMT", zone.getShortName(TEST_TIME_WINTER));
        assertEquals("BST", zone.getShortName(TEST_TIME_SUMMER, Locale.ENGLISH));
    }

    public void testGetShortName_berlin() {
        DateTimeZone berlin = DateTimeZone.forID("Europe/Berlin");
        assertEquals("CET", berlin.getShortName(TEST_TIME_WINTER, Locale.ENGLISH));
        assertEquals("CEST", berlin.getShortName(TEST_TIME_SUMMER, Locale.ENGLISH));
        if (JDK6PLUS) {
          assertEquals("MEZ", berlin.getShortName(TEST_TIME_WINTER, Locale.GERMAN));
          assertEquals("MESZ", berlin.getShortName(TEST_TIME_SUMMER, Locale.GERMAN));
        } else {
          assertEquals("CET", berlin.getShortName(TEST_TIME_WINTER, Locale.GERMAN));
          assertEquals("CEST", berlin.getShortName(TEST_TIME_SUMMER, Locale.GERMAN));
        }
    }
    */

    @Test
    public void testGetShortNameProviderName() {
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(null, "Europe/London", "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(Locale.ENGLISH, null, "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(Locale.ENGLISH, "Europe/London", null));
        assertEquals(null, DateTimeZone.getNameProvider().getShortName(null, null, null));
    }

    @Test
    public void testGetShortNameNullKey() {
        DateTimeZone zone = new MockDateTimeZone("Europe/London");
        assertEquals("Europe/London", zone.getShortName(TEST_TIME_SUMMER, Locale.ENGLISH));
    }

    // Names are not the same between JDK and Android
    /*
    public void testGetName() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals("British Summer Time", zone.getName(TEST_TIME_SUMMER));
        assertEquals("Greenwich Mean Time", zone.getName(TEST_TIME_WINTER));
        assertEquals("British Summer Time", zone.getName(TEST_TIME_SUMMER, Locale.ENGLISH));
    }

    public void testGetName_berlin_english() {
      DateTimeZone berlin = DateTimeZone.forID("Europe/Berlin");
      if (JDK9) {
          assertEquals("Central European Standard Time", berlin.getName(TEST_TIME_WINTER, Locale.ENGLISH));
      } else {
          assertEquals("Central European Time", berlin.getName(TEST_TIME_WINTER, Locale.ENGLISH));
      }
      assertEquals("Central European Summer Time", berlin.getName(TEST_TIME_SUMMER, Locale.ENGLISH));
    }

    public void testGetName_berlin_german() {
        DateTimeZone berlin = DateTimeZone.forID("Europe/Berlin");
        if (JDK9) {
            assertEquals("Mitteleurop\u00e4ische Normalzeit", berlin.getName(TEST_TIME_WINTER, Locale.GERMAN));
            assertEquals("Mitteleurop\u00e4ische Sommerzeit", berlin.getName(TEST_TIME_SUMMER, Locale.GERMAN));
        } else if (JDK6PLUS) {
            assertEquals("Mitteleurop\u00e4ische Zeit", berlin.getName(TEST_TIME_WINTER, Locale.GERMAN));
            assertEquals("Mitteleurop\u00e4ische Sommerzeit", berlin.getName(TEST_TIME_SUMMER, Locale.GERMAN));
        } else {
            assertEquals("Zentraleurop\u00e4ische Zeit", berlin.getName(TEST_TIME_WINTER, Locale.GERMAN));
            assertEquals("Zentraleurop\u00e4ische Sommerzeit", berlin.getName(TEST_TIME_SUMMER, Locale.GERMAN));
        }
    }
  */

    @Test
    public void testGetNameProviderName() {
        assertEquals(null, DateTimeZone.getNameProvider().getName(null, "Europe/London", "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getName(Locale.ENGLISH, null, "BST"));
        assertEquals(null, DateTimeZone.getNameProvider().getName(Locale.ENGLISH, "Europe/London", null));
        assertEquals(null, DateTimeZone.getNameProvider().getName(null, null, null));
    }

    @Test
    public void testGetNameNullKey() {
        DateTimeZone zone = new MockDateTimeZone("Europe/London");
        assertEquals("Europe/London", zone.getName(TEST_TIME_SUMMER, Locale.ENGLISH));
    }

    static class MockDateTimeZone extends DateTimeZone {
        public MockDateTimeZone(String id) {
            super(id);
        }
        public String getNameKey(long instant) {
            return null;  // null
        }
        public int getOffset(long instant) {
            return 0;
        }
        public int getStandardOffset(long instant) {
            return 0;
        }
        public boolean isFixed() {
            return false;
        }
        public long nextTransition(long instant) {
            return 0;
        }
        public long previousTransition(long instant) {
            return 0;
        }
        public boolean equals(Object object) {
            return false;
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testGetOffset_long() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_WINTER));
        
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_WINTER));
        
        assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_WINTER));
        
        assertEquals(false, zone.isStandardOffset(TEST_TIME_SUMMER));
        assertEquals(true, zone.isStandardOffset(TEST_TIME_WINTER));
    }

    @Test
    public void testGetOffset_RI() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_SUMMER)));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_WINTER)));
        
        assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset(null));
    }

    @Test
    public void testGetOffsetFixed() {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_WINTER));
        
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_WINTER));
        
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_SUMMER));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_WINTER));
        
        assertEquals(true, zone.isStandardOffset(TEST_TIME_SUMMER));
        assertEquals(true, zone.isStandardOffset(TEST_TIME_WINTER));
    }

    @Test
    public void testGetOffsetFixed_RI() {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_SUMMER)));
        assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_WINTER)));
        
        assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset(null));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testGetMillisKeepLocal() {
        long millisLondon = TEST_TIME_SUMMER;
        long millisParis = TEST_TIME_SUMMER - 1L * DateTimeConstants.MILLIS_PER_HOUR;
        
        assertEquals(millisLondon, LONDON.getMillisKeepLocal(LONDON, millisLondon));
        assertEquals(millisParis, LONDON.getMillisKeepLocal(LONDON, millisParis));
        assertEquals(millisLondon, PARIS.getMillisKeepLocal(PARIS, millisLondon));
        assertEquals(millisParis, PARIS.getMillisKeepLocal(PARIS, millisParis));
        
        assertEquals(millisParis, LONDON.getMillisKeepLocal(PARIS, millisLondon));
        assertEquals(millisLondon, PARIS.getMillisKeepLocal(LONDON, millisParis));
        
        DateTimeZone zone = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(LONDON);
            assertEquals(millisLondon, PARIS.getMillisKeepLocal(null, millisParis));
        } finally {
            DateTimeZone.setDefault(zone);
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testIsFixed() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals(false, zone.isFixed());
        assertEquals(true, DateTimeZone.UTC.isFixed());
    }

    //-----------------------------------------------------------------------
    @Test
    public void testTransitionFixed() {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        assertEquals(TEST_TIME_SUMMER, zone.nextTransition(TEST_TIME_SUMMER));
        assertEquals(TEST_TIME_WINTER, zone.nextTransition(TEST_TIME_WINTER));
        assertEquals(TEST_TIME_SUMMER, zone.previousTransition(TEST_TIME_SUMMER));
        assertEquals(TEST_TIME_WINTER, zone.previousTransition(TEST_TIME_WINTER));
    }

//    //-----------------------------------------------------------------------
//    public void testIsLocalDateTimeOverlap_Berlin() {
//        DateTimeZone zone = DateTimeZone.forID("Europe/Berlin");
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 1, 0)));
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 1, 59, 59, 99)));
//        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 2, 0)));
//        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 2, 30)));
//        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 2, 59, 59, 99)));
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 3, 0)));
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 4, 0)));
//        
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 25, 1, 30)));  // before gap
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 25, 2, 30)));  // gap
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 25, 3, 30)));  // after gap
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 12, 24, 12, 34)));
//    }
//
//    //-----------------------------------------------------------------------
//    public void testIsLocalDateTimeOverlap_NewYork() {
//        DateTimeZone zone = DateTimeZone.forID("America/New_York");
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 0, 0)));
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 0, 59, 59, 99)));
//        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 1, 0)));
//        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 1, 30)));
//        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 1, 59, 59, 99)));
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 2, 0)));
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 3, 0)));
//        
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 11, 1, 30)));  // before gap
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 11, 2, 30)));  // gap
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 11, 3, 30)));  // after gap
//        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 12, 24, 12, 34)));
//    }

    //-----------------------------------------------------------------------
    @Test
    public void testIsLocalDateTimeGap_Berlin() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Berlin");
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 1, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 1, 59, 59, 99)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 0)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 30)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 59, 59, 99)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 3, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 4, 0)));
        
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 1, 30)));  // before overlap
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 2, 30)));  // overlap
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 3, 30)));  // after overlap
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 12, 24, 12, 34)));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testIsLocalDateTimeGap_NewYork() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 1, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 1, 59, 59, 99)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 0)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 30)));
        assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 59, 59, 99)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 3, 0)));
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 4, 0)));
        
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 0, 30)));  // before overlap
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 1, 30)));  // overlap
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 2, 30)));  // after overlap
        assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 12, 24, 12, 34)));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        TimeZone tz = zone.toTimeZone();
        assertEquals("Europe/Paris", tz.getID());
    }

    //-----------------------------------------------------------------------
    @Test
    public void testEqualsHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Paris");
        assertEquals(true, zone1.equals(zone1));
        assertEquals(true, zone1.equals(zone2));
        assertEquals(true, zone2.equals(zone1));
        assertEquals(true, zone2.equals(zone2));
        assertEquals(true, zone1.hashCode() == zone2.hashCode());
        
        DateTimeZone zone3 = DateTimeZone.forID("Europe/London");
        assertEquals(true, zone3.equals(zone3));
        assertEquals(false, zone1.equals(zone3));
        assertEquals(false, zone2.equals(zone3));
        assertEquals(false, zone3.equals(zone1));
        assertEquals(false, zone3.equals(zone2));
        assertEquals(false, zone1.hashCode() == zone3.hashCode());
        assertEquals(true, zone3.hashCode() == zone3.hashCode());
        
        DateTimeZone zone4 = DateTimeZone.forID("+01:00");
        assertEquals(true, zone4.equals(zone4));
        assertEquals(false, zone1.equals(zone4));
        assertEquals(false, zone2.equals(zone4));
        assertEquals(false, zone3.equals(zone4));
        assertEquals(false, zone4.equals(zone1));
        assertEquals(false, zone4.equals(zone2));
        assertEquals(false, zone4.equals(zone3));
        assertEquals(false, zone1.hashCode() == zone4.hashCode());
        assertEquals(true, zone4.hashCode() == zone4.hashCode());
        
        DateTimeZone zone5 = DateTimeZone.forID("+02:00");
        assertEquals(true, zone5.equals(zone5));
        assertEquals(false, zone1.equals(zone5));
        assertEquals(false, zone2.equals(zone5));
        assertEquals(false, zone3.equals(zone5));
        assertEquals(false, zone4.equals(zone5));
        assertEquals(false, zone5.equals(zone1));
        assertEquals(false, zone5.equals(zone2));
        assertEquals(false, zone5.equals(zone3));
        assertEquals(false, zone5.equals(zone4));
        assertEquals(false, zone1.hashCode() == zone5.hashCode());
        assertEquals(true, zone5.hashCode() == zone5.hashCode());
    }

    //-----------------------------------------------------------------------
    @Test
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals("Europe/Paris", zone.toString());
        assertEquals("UTC", DateTimeZone.UTC.toString());
    }

    //-----------------------------------------------------------------------
    @Test
    public void testDublin() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Dublin");
        DateTime winter = new DateTime(2018, 1, 1, 0, 0, 0, 0, zone);
        assertEquals(0, zone.getStandardOffset(winter.getMillis()));
        assertEquals(0, zone.getOffset(winter.getMillis()));
        assertEquals(true, zone.isStandardOffset(winter.getMillis()));
        assertEquals("Greenwich Mean Time", zone.getName(winter.getMillis()));
        assertEquals("GMT", zone.getNameKey(winter.getMillis()));

        DateTime summer = winter.plusMonths(6);
        assertEquals(0, zone.getStandardOffset(summer.getMillis()));
        assertEquals(3600000, zone.getOffset(summer.getMillis()));
        assertEquals(false, zone.isStandardOffset(summer.getMillis()));
        assertEquals(true, zone.getName(summer.getMillis()).startsWith("Irish "));
        assertEquals("IST", zone.getNameKey(summer.getMillis()));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWindhoek() {
        DateTimeZone zone = DateTimeZone.forID("Africa/Windhoek");
        DateTime dtDec1990 = new DateTime(1990, 12, 1, 0, 0, 0, 0, zone);
        assertEquals(3600000, zone.getStandardOffset(dtDec1990.getMillis()));
        assertEquals(7200000, zone.getOffset(dtDec1990.getMillis()));
        assertEquals(false, zone.isStandardOffset(dtDec1990.getMillis()));

        DateTime dtDec1994 = new DateTime(1994, 12, 1, 0, 0, 0, 0, zone);
        assertEquals(3600000, zone.getStandardOffset(dtDec1994.getMillis()));
        assertEquals(7200000, zone.getOffset(dtDec1994.getMillis()));
        assertEquals(false, zone.isStandardOffset(dtDec1994.getMillis()));

        DateTime dtJun1995 = new DateTime(1995, 6, 1, 0, 0, 0, 0, zone);
        assertEquals(3600000, zone.getStandardOffset(dtJun1995.getMillis()));
        assertEquals(3600000, zone.getOffset(dtJun1995.getMillis()));
        assertEquals(true, zone.isStandardOffset(dtJun1995.getMillis()));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testSerialization1() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone result = (DateTimeZone) ois.readObject();
        ois.close();
        
        assertSame(zone, result);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testSerialization2() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone result = (DateTimeZone) ois.readObject();
        ois.close();
        
        assertEquals(zone, result);
    }

    @Test
    public void testCommentParse() throws Exception {
        // A bug in ZoneInfoCompiler's handling of comments broke Europe/Athens
        // after 1980. This test is included to make sure it doesn't break again.

        DateTimeZone zone = DateTimeZone.forID("Europe/Athens");
        DateTime dt = new DateTime(2005, 5, 5, 20, 10, 15, 0, zone);
        assertEquals(1115313015000L, dt.getMillis());
    }

    @Test
    public void testPatchedNameKeysLondon() throws Exception {
        // the tz database does not have unique name keys [1716305]
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        
        DateTime now = new DateTime(2007, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
    }

    @Test
    public void testPatchedNameKeysSydney() throws Exception {
        // the tz database does not have unique name keys [1716305]
        DateTimeZone zone = DateTimeZone.forID("Australia/Sydney");
        
        DateTime now = new DateTime(2007, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
    }

    @Test
    public void testPatchedNameKeysSydneyHistoric() throws Exception {
        // the tz database does not have unique name keys [1716305]
        DateTimeZone zone = DateTimeZone.forID("Australia/Sydney");
        
        DateTime now = new DateTime(1996, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
    }

    @Test
    public void testPatchedNameKeysGazaHistoric() throws Exception {
        // the tz database does not have unique name keys [1716305]
        DateTimeZone zone = DateTimeZone.forID("Africa/Johannesburg");
        
        DateTime now = new DateTime(1943, 1, 1, 0, 0, 0, 0);
        String str1 = zone.getName(now.getMillis());
        String str2 = zone.getName(now.plusMonths(6).getMillis());
        assertEquals(false, str1.equals(str2));
    }

}
