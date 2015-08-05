package net.danlew.android.joda.test;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.Settings;
import android.test.InstrumentationTestCase;
import net.danlew.android.joda.DateUtils;
import net.danlew.android.joda.JodaTimeAndroid;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MonthDay;
import org.joda.time.YearMonth;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Test our implementation of DateUtils methods against the actual Android
 * DateUtils implementation.
 */
public class TestDateUtils extends InstrumentationTestCase {

    /**
     * These are all the different flags we'll pass into formatDateTime()/formatDateRange() for testing
     */
    private static final int[] FORMAT_DATE_RANGE_FLAGS = {
        DateUtils.FORMAT_SHOW_DATE,
        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH,
        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE,
        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL,
        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME,
        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY,
        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY,
        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR,
        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY,
        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_NO_YEAR,
        DateUtils.FORMAT_SHOW_TIME,
        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_NOON,
        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_MIDNIGHT,
        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_TIME,
        DateUtils.FORMAT_SHOW_WEEKDAY,
        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY,
        DateUtils.FORMAT_SHOW_YEAR,
    };

    // Values that will represent "now" during our tests
    private static final int YEAR = 1995;
    private static final int MONTH_OF_YEAR = 10;
    private static final int DAY_OF_MONTH = 22;
    private static final int HOUR_OF_DAY = 12;
    private static final int MINUTE_OF_HOUR = 35;
    private static final int SECOND_OF_MINUTE = 20;
    private static final int MILLIS_OF_SECOND = 103;

    private String mOldTime1224Setting;

    private DateTime mNow;
    private DateTimeZone mDefaultJodaTz;
    private DateTimeZone mOldDefaultJodaTz;

    private TimeZone mDefaultSystemTz;
    private TimeZone mOldDefaultSystemTz;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Init zone info
        Context context = getInstrumentation().getContext();
        JodaTimeAndroid.init(context);

        // Force the system into 24-hour time for tests
        ContentResolver cr = context.getContentResolver();
        mOldTime1224Setting = Settings.System.getString(cr, Settings.System.TIME_12_24);
        Settings.System.putString(cr, Settings.System.TIME_12_24, "24");

        // Force all tests to be in the US locale; that way we can test output in consistent manner
        Application app = (Application) getInstrumentation().getContext().getApplicationContext();
        Resources res = app.getBaseContext().getResources();
        Configuration config = res.getConfiguration();
        Locale.setDefault(Locale.US);
        config.locale = Locale.US;
        res.updateConfiguration(config, res.getDisplayMetrics());

        // Force the default timezone
        mDefaultJodaTz = DateTimeZone.forID("America/New_York");
        mOldDefaultJodaTz = DateTimeZone.getDefault();
        DateTimeZone.setDefault(mDefaultJodaTz);

        // ...And for the system as well
        mDefaultSystemTz = TimeZone.getTimeZone("America/Chicago");
        mOldDefaultSystemTz = TimeZone.getDefault();
        TimeZone.setDefault(mDefaultSystemTz);

        // Force current "now" time, so all tests can be consistent
        mNow = new DateTime(YEAR, MONTH_OF_YEAR, DAY_OF_MONTH, HOUR_OF_DAY,
            MINUTE_OF_HOUR, SECOND_OF_MINUTE, MILLIS_OF_SECOND, mDefaultJodaTz);
        DateTimeUtils.setCurrentMillisFixed(mNow.getMillis());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // Restore to normal "now" time
        DateTimeUtils.setCurrentMillisSystem();
        DateTimeZone.setDefault(mOldDefaultJodaTz);
        TimeZone.setDefault(mOldDefaultSystemTz);
        ContentResolver cr = getInstrumentation().getContext().getContentResolver();
        Settings.System.putString(cr, Settings.System.TIME_12_24, mOldTime1224Setting);
    }

    public void testFormatDateTime() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("America/Chicago"));
        cal.set(1985, 10, 27, 5, 23, 5);
        long millis = cal.getTimeInMillis();

        DateTime dateTime = new DateTime(1985, 11, 27, 5, 23, 5, DateTimeZone.forID("America/Chicago"));

        Context ctx = getInstrumentation().getContext();
        for (int a = 0; a < FORMAT_DATE_RANGE_FLAGS.length; a++) {
            int flags = FORMAT_DATE_RANGE_FLAGS[a];
            assertEquals(android.text.format.DateUtils.formatDateTime(ctx, millis, flags),
                DateUtils.formatDateTime(ctx, dateTime, flags));
        }
    }

    public void testFormatDateTimePartial() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("America/Chicago"));
        cal.set(1985, 10, 27, 0, 0, 0);
        long millis = cal.getTimeInMillis();

        LocalDate localDate = new LocalDate(1985, 11, 27);

        Context ctx = getInstrumentation().getContext();
        for (int a = 0; a < FORMAT_DATE_RANGE_FLAGS.length; a++) {
            int flags = FORMAT_DATE_RANGE_FLAGS[a];
            assertEquals(android.text.format.DateUtils.formatDateTime(ctx, millis, flags),
                DateUtils.formatDateTime(ctx, localDate, flags));
        }
    }

    public void testFormatDateRange() {
        Calendar startPartialCal = new GregorianCalendar(TimeZone.getTimeZone("America/Chicago"));
        startPartialCal.set(1985, 10, 27, 0, 0, 0);
        long startPartialMs = startPartialCal.getTimeInMillis();

        Calendar endPartialCal = new GregorianCalendar(TimeZone.getTimeZone("America/Chicago"));
        endPartialCal.set(1985, 11, 25, 0, 0, 0);
        long endPartialMs = endPartialCal.getTimeInMillis() + 1000; // Include buffer for formatDateRange() bug

        LocalDate startLocalDate = new LocalDate(1985, 11, 27);
        LocalDate endLocalDate = new LocalDate(1985, 12, 25);

        Calendar startCal = new GregorianCalendar(TimeZone.getTimeZone("America/Chicago"));
        startCal.set(1985, 10, 27, 5, 23, 5);
        long startMillis = startCal.getTimeInMillis();

        Calendar endCal = new GregorianCalendar(TimeZone.getTimeZone("America/Chicago"));
        endCal.set(1985, 11, 25, 20, 14, 25);
        long endMillis = endCal.getTimeInMillis();

        DateTime startDateTime = new DateTime(1985, 11, 27, 5, 23, 5, DateTimeZone.forID("America/Chicago"));
        DateTime endDateTime = new DateTime(1985, 12, 25, 20, 14, 25, DateTimeZone.forID("America/Chicago"));

        Context ctx = getInstrumentation().getContext();

        for (int a = 0; a < FORMAT_DATE_RANGE_FLAGS.length; a++) {
            int flags = FORMAT_DATE_RANGE_FLAGS[a];

            // Start partial, end partial
            assertEquals(android.text.format.DateUtils.formatDateRange(ctx, startPartialMs, endPartialMs, flags),
                DateUtils.formatDateRange(ctx, startLocalDate, endLocalDate, flags));

            // Start instant, end instant
            assertEquals(android.text.format.DateUtils.formatDateRange(ctx, startMillis, endMillis, flags),
                DateUtils.formatDateRange(ctx, startDateTime, endDateTime, flags));

            // Same start/end time
            assertEquals(android.text.format.DateUtils.formatDateRange(ctx, startPartialMs, startPartialMs, flags),
                DateUtils.formatDateRange(ctx, startLocalDate, startLocalDate, flags));
            assertEquals(android.text.format.DateUtils.formatDateRange(ctx, startMillis, startMillis, flags),
                DateUtils.formatDateRange(ctx, startDateTime, startDateTime, flags));
        }
    }

    public void testFormatElapsedTime() {
        assertEquals("00:00", DateUtils.formatElapsedTime(Duration.standardSeconds(0)));
        assertEquals("00:05", DateUtils.formatElapsedTime(Duration.standardSeconds(5)));
        assertEquals("00:15", DateUtils.formatElapsedTime(Duration.standardSeconds(15)));
        assertEquals("02:30", DateUtils.formatElapsedTime(Duration.standardSeconds(150)));
        assertEquals("33:20", DateUtils.formatElapsedTime(Duration.standardSeconds(2000)));
        assertEquals("4:10:00", DateUtils.formatElapsedTime(Duration.standardSeconds(15000)));
        assertEquals("292:35:33", DateUtils.formatElapsedTime(Duration.standardSeconds(1053333)));
    }

    public void testIsToday() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        assertEquals(true, DateUtils.isToday(today));
        assertEquals(false, DateUtils.isToday(yesterday));
        assertEquals(false, DateUtils.isToday(tomorrow));

        LocalDateTime todayLdt = LocalDateTime.now();
        LocalDateTime yesterdayLdt = todayLdt.minusDays(1);
        LocalDateTime tomorrowLdt = todayLdt.plusDays(1);

        assertEquals(true, DateUtils.isToday(todayLdt));
        assertEquals(false, DateUtils.isToday(yesterdayLdt));
        assertEquals(false, DateUtils.isToday(tomorrowLdt));

        DateTime todayDt = DateTime.now();
        DateTime yesterdayDt = todayDt.minusDays(1);
        DateTime tomorrowDt = todayDt.plusDays(1);

        assertEquals(true, DateUtils.isToday(todayDt));
        assertEquals(false, DateUtils.isToday(yesterdayDt));
        assertEquals(false, DateUtils.isToday(tomorrowDt));

        try {
            DateUtils.isToday(new MonthDay());
            fail("DateUtils.isToday() should have thrown an error since MonthDay has no year.");
        }
        catch (Exception e) {

        }

        try {
            DateUtils.isToday(new YearMonth());
            fail("DateUtils.isToday() should have thrown an error since YearMonth has no day.");
        }
        catch (Exception e) {

        }
    }

    public void testGetRelativeTimeSpanString() {
        Context ctx = getInstrumentation().getContext();

        // Test all output strings
        assertEquals("in 1 second", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusSeconds(1)));
        assertEquals("in 30 seconds", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusSeconds(30)));
        assertEquals("1 second ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusSeconds(1)));
        assertEquals("30 seconds ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusSeconds(30)));
        assertEquals("in 1 sec", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusSeconds(1),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("in 30 secs", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusSeconds(30),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("1 sec ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusSeconds(1),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("30 secs ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusSeconds(30),
            DateUtils.FORMAT_ABBREV_RELATIVE));

        assertEquals("in 1 minute", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusMinutes(1)));
        assertEquals("in 30 minutes", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusMinutes(30)));
        assertEquals("1 minute ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusMinutes(1)));
        assertEquals("30 minutes ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusMinutes(30)));
        assertEquals("in 1 min", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusMinutes(1),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("in 30 mins", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusMinutes(30),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("1 min ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusMinutes(1),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("30 mins ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusMinutes(30),
            DateUtils.FORMAT_ABBREV_RELATIVE));

        assertEquals("in 1 hour", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusHours(1)));
        assertEquals("in 3 hours", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusHours(3)));
        assertEquals("1 hour ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusHours(1)));
        assertEquals("3 hours ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusHours(3)));
        assertEquals("in 1 hour", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusHours(1),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("in 3 hours", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusHours(3),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("1 hour ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusHours(1),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("3 hours ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusHours(3),
            DateUtils.FORMAT_ABBREV_RELATIVE));

        assertEquals("tomorrow", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusDays(1)));
        assertEquals("in 3 days", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusDays(3)));
        assertEquals("yesterday", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusDays(1)));
        assertEquals("3 days ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusDays(3)));
        assertEquals("tomorrow", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusDays(1),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("in 3 days", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusDays(3),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("yesterday", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusDays(1),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("3 days ago", DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusDays(3),
            DateUtils.FORMAT_ABBREV_RELATIVE));

        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH;
        assertEquals(DateUtils.formatDateTime(ctx, DateTime.now().plusWeeks(1), flags),
            DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().plusWeeks(1)));
        assertEquals(DateUtils.formatDateTime(ctx, DateTime.now().minusWeeks(1), flags),
            DateUtils.getRelativeTimeSpanString(ctx, DateTime.now().minusWeeks(1)));

        // Test partial inputs
        assertEquals("tomorrow", DateUtils.getRelativeTimeSpanString(ctx, LocalDate.now().plusDays(1)));
        assertEquals("in 3 days", DateUtils.getRelativeTimeSpanString(ctx, LocalDate.now().plusDays(3)));
        assertEquals("yesterday", DateUtils.getRelativeTimeSpanString(ctx, LocalDate.now().minusDays(1)));
        assertEquals("3 days ago", DateUtils.getRelativeTimeSpanString(ctx, LocalDate.now().minusDays(3)));

        assertEquals("in 1 hour", DateUtils.getRelativeTimeSpanString(ctx, LocalTime.now().plusHours(1)));
        assertEquals("in 3 hours", DateUtils.getRelativeTimeSpanString(ctx, LocalTime.now().plusHours(3)));
        assertEquals("1 hour ago", DateUtils.getRelativeTimeSpanString(ctx, LocalTime.now().minusHours(1)));
        assertEquals("3 hours ago", DateUtils.getRelativeTimeSpanString(ctx, LocalTime.now().minusHours(3)));

        assertEquals("in 1 min", DateUtils.getRelativeTimeSpanString(ctx, LocalTime.now().plusMinutes(1),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("in 30 mins", DateUtils.getRelativeTimeSpanString(ctx, LocalTime.now().plusMinutes(30),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("1 min ago", DateUtils.getRelativeTimeSpanString(ctx, LocalTime.now().minusMinutes(1),
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("30 mins ago", DateUtils.getRelativeTimeSpanString(ctx, LocalTime.now().minusMinutes(30),
            DateUtils.FORMAT_ABBREV_RELATIVE));
    }

    public void testGetRelativeTimeSpanStringWithPreposition() {
        Context ctx = getInstrumentation().getContext();

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextYear = today.plusYears(1);

        assertEquals("12:35", DateUtils.getRelativeTimeSpanString(ctx, today, false));
        assertEquals("at 12:35", DateUtils.getRelativeTimeSpanString(ctx, today, true));
        assertEquals("Oct 23, 1995", DateUtils.getRelativeTimeSpanString(ctx, tomorrow, false));
        assertEquals("on Oct 23, 1995", DateUtils.getRelativeTimeSpanString(ctx, tomorrow, true));
        assertEquals("10/22/1996", DateUtils.getRelativeTimeSpanString(ctx, nextYear, false));
        assertEquals("on 10/22/1996", DateUtils.getRelativeTimeSpanString(ctx, nextYear, true));

        DateTime todayDt = DateTime.now();
        DateTime tomorrowDt = todayDt.plusDays(1);
        DateTime nextYearDt = todayDt.plusYears(1);

        assertEquals("12:35", DateUtils.getRelativeTimeSpanString(ctx, todayDt, false));
        assertEquals("at 12:35", DateUtils.getRelativeTimeSpanString(ctx, todayDt, true));
        assertEquals("Oct 23, 1995", DateUtils.getRelativeTimeSpanString(ctx, tomorrowDt, false));
        assertEquals("on Oct 23, 1995", DateUtils.getRelativeTimeSpanString(ctx, tomorrowDt, true));
        assertEquals("10/22/1996", DateUtils.getRelativeTimeSpanString(ctx, nextYearDt, false));
        assertEquals("on 10/22/1996", DateUtils.getRelativeTimeSpanString(ctx, nextYearDt, true));
    }

    public void testGetRelativeDateTimeString() {
        Context ctx = getInstrumentation().getContext();

        assertEquals("0 seconds ago, 12:35", DateUtils.getRelativeDateTimeString(ctx, mNow, null, 0));
        assertEquals("in 30 seconds, 12:35", DateUtils.getRelativeDateTimeString(ctx, mNow.plusSeconds(30), null, 0));
        assertEquals("30 seconds ago, 12:34", DateUtils.getRelativeDateTimeString(ctx, mNow.minusSeconds(30), null, 0));
        assertEquals("in 30 minutes, 13:05", DateUtils.getRelativeDateTimeString(ctx, mNow.plusMinutes(30), null, 0));
        assertEquals("30 minutes ago, 12:05", DateUtils.getRelativeDateTimeString(ctx, mNow.minusMinutes(30), null, 0));
        assertEquals("in 3 hours, 15:35", DateUtils.getRelativeDateTimeString(ctx, mNow.plusHours(3), null, 0));
        assertEquals("3 hours ago, 09:35", DateUtils.getRelativeDateTimeString(ctx, mNow.minusHours(3), null, 0));
        assertEquals("Oct 25, 1995, 12:35", DateUtils.getRelativeDateTimeString(ctx, mNow.plusDays(3), null, 0));
        assertEquals("Oct 19, 1995, 12:35", DateUtils.getRelativeDateTimeString(ctx, mNow.minusDays(3), null, 0));

        // Test abbreviation
        assertEquals("in 30 secs, 12:35",
            DateUtils.getRelativeDateTimeString(ctx, mNow.plusSeconds(30), null, DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("30 secs ago, 12:34",
            DateUtils.getRelativeDateTimeString(ctx, mNow.minusSeconds(30), null, DateUtils.FORMAT_ABBREV_RELATIVE));

        // Test transition resolution
        DateTime dt = DateTime.now().plusDays(2);
        assertEquals("in 2 days, 12:35", DateUtils.getRelativeDateTimeString(ctx, dt, Days.TWO, 0));
        assertEquals("Oct 24, 1995, 12:35", DateUtils.getRelativeDateTimeString(ctx, dt.plusSeconds(1), Days.TWO, 0));
        assertEquals("in 2 days, 12:35", DateUtils.getRelativeDateTimeString(ctx, dt, Days.THREE, 0));

        // Test partial input
        LocalTime lt = LocalTime.now();
        assertEquals("in 30 seconds, 12:35", DateUtils.getRelativeDateTimeString(ctx, lt.plusSeconds(30), null, 0));
        assertEquals("30 seconds ago, 12:34", DateUtils.getRelativeDateTimeString(ctx, lt.minusSeconds(30), null, 0));
        assertEquals("in 30 minutes, 13:05", DateUtils.getRelativeDateTimeString(ctx, lt.plusMinutes(30), null, 0));
        assertEquals("30 minutes ago, 12:05", DateUtils.getRelativeDateTimeString(ctx, lt.minusMinutes(30), null, 0));

        // Test bad partial input
        try {
            assertEquals("Oct 24, 1995, 12:35", DateUtils.getRelativeDateTimeString(ctx, LocalDate.now(), null, 0));
            fail("DateUtils.getRelativeDateTimeString() should have thrown an error since LocalDate has no time.");
        }
        catch (Exception e) {

        }
    }

    public void testFormatDuration() {
        Context ctx = getInstrumentation().getContext();

        assertEquals("1 second", DateUtils.formatDuration(ctx, Duration.standardSeconds(1)));
        assertEquals("-1 seconds", DateUtils.formatDuration(ctx, Duration.standardSeconds(-1)));
        assertEquals("30 seconds", DateUtils.formatDuration(ctx, Duration.standardSeconds(30)));
        assertEquals("-30 seconds", DateUtils.formatDuration(ctx, Duration.standardSeconds(-30)));
        assertEquals("1 minute", DateUtils.formatDuration(ctx, Duration.standardMinutes(1)));
        assertEquals("-1 minutes", DateUtils.formatDuration(ctx, Duration.standardMinutes(-1)));
        assertEquals("30 minutes", DateUtils.formatDuration(ctx, Duration.standardMinutes(30)));
        assertEquals("-30 minutes", DateUtils.formatDuration(ctx, Duration.standardMinutes(-30)));
        assertEquals("1 hour", DateUtils.formatDuration(ctx, Duration.standardHours(1)));
        assertEquals("-1 hours", DateUtils.formatDuration(ctx, Duration.standardHours(-1)));
        assertEquals("12 hours", DateUtils.formatDuration(ctx, Duration.standardHours(12)));
        assertEquals("-12 hours", DateUtils.formatDuration(ctx, Duration.standardHours(-12)));
    }
}
