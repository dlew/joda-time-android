package net.danlew.android.joda.test;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.test.InstrumentationTestCase;
import net.danlew.android.joda.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Force all tests to be in the US locale; that way we can test output in consistent manner
        Application app = (Application) getInstrumentation().getContext().getApplicationContext();
        Resources res = app.getBaseContext().getResources();
        Configuration config = res.getConfiguration();
        Locale.setDefault(Locale.US);
        config.locale = Locale.US;
        res.updateConfiguration(config, res.getDisplayMetrics());
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

            // Start partial, end instant
            assertEquals(android.text.format.DateUtils.formatDateRange(ctx, startPartialMs, endMillis, flags),
                DateUtils.formatDateRange(ctx, startLocalDate, endDateTime, flags));

            // Start instant, end partial
            assertEquals(android.text.format.DateUtils.formatDateRange(ctx, startMillis, endPartialMs, flags),
                DateUtils.formatDateRange(ctx, startDateTime, endLocalDate, flags));

            // Start instant, end instant
            assertEquals(android.text.format.DateUtils.formatDateRange(ctx, startMillis, endMillis, flags),
                DateUtils.formatDateRange(ctx, startDateTime, endDateTime, flags));
        }
    }

    public void testFormatElapsedTime() {
        int[] secondsToTest = {
            0, 5, 15, 150, 2000, 15000, 1053333
        };

        for (int a = 0; a < secondsToTest.length; a++) {
            assertEquals(android.text.format.DateUtils.formatElapsedTime(secondsToTest[a]),
                DateUtils.formatElapsedTime(Duration.standardSeconds(secondsToTest[a])));
        }
    }

    public void testIsToday() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        assertEquals(android.text.format.DateUtils.isToday(today.toDateTimeAtStartOfDay().getMillis()),
            DateUtils.isToday(today));
        assertEquals(android.text.format.DateUtils.isToday(yesterday.toDateTimeAtStartOfDay().getMillis()),
            DateUtils.isToday(yesterday));
        assertEquals(android.text.format.DateUtils.isToday(tomorrow.toDateTimeAtStartOfDay().getMillis()),
            DateUtils.isToday(tomorrow));

        LocalDateTime todayLdt = LocalDateTime.now();
        LocalDateTime yesterdayLdt = todayLdt.minusDays(1);
        LocalDateTime tomorrowLdt = todayLdt.plusDays(1);

        assertEquals(android.text.format.DateUtils.isToday(todayLdt.toDateTime().getMillis()),
            DateUtils.isToday(todayLdt));
        assertEquals(android.text.format.DateUtils.isToday(yesterdayLdt.toDateTime().getMillis()),
            DateUtils.isToday(yesterdayLdt));
        assertEquals(android.text.format.DateUtils.isToday(tomorrowLdt.toDateTime().getMillis()),
            DateUtils.isToday(tomorrowLdt));

        DateTime todayDt = DateTime.now();
        DateTime yesterdayDt = todayDt.minusDays(1);
        DateTime tomorrowDt = todayDt.plusDays(1);

        assertEquals(android.text.format.DateUtils.isToday(todayDt.getMillis()),
            DateUtils.isToday(todayDt));
        assertEquals(android.text.format.DateUtils.isToday(yesterdayDt.getMillis()),
            DateUtils.isToday(yesterdayDt));
        assertEquals(android.text.format.DateUtils.isToday(tomorrowDt.getMillis()),
            DateUtils.isToday(tomorrowDt));

        Calendar todayCal = Calendar.getInstance();
        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.add(Calendar.DAY_OF_MONTH, -1);
        Calendar tomorrowCal = Calendar.getInstance();
        tomorrowCal.add(Calendar.DAY_OF_MONTH, 1);

        assertEquals(android.text.format.DateUtils.isToday(todayCal.getTimeInMillis()),
            DateUtils.isToday(new LocalDate(todayCal)));
        assertEquals(android.text.format.DateUtils.isToday(todayCal.getTimeInMillis()),
            DateUtils.isToday(new DateTime(todayCal)));
        assertEquals(android.text.format.DateUtils.isToday(yesterdayCal.getTimeInMillis()),
            DateUtils.isToday(new LocalDate(yesterdayCal)));
        assertEquals(android.text.format.DateUtils.isToday(yesterdayCal.getTimeInMillis()),
            DateUtils.isToday(new DateTime(yesterdayCal)));
        assertEquals(android.text.format.DateUtils.isToday(tomorrowCal.getTimeInMillis()),
            DateUtils.isToday(new LocalDate(tomorrowCal)));
        assertEquals(android.text.format.DateUtils.isToday(tomorrowCal.getTimeInMillis()),
            DateUtils.isToday(new DateTime(tomorrowCal)));

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

        DateTime dateTime = new DateTime(1988, 3, 4, 5, 6, 15, DateTimeZone.UTC);

        // Test all output strings
        assertEquals("in 1 second", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusSeconds(1), dateTime));
        assertEquals("in 30 seconds", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusSeconds(30), dateTime));
        assertEquals("1 second ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusSeconds(1), dateTime));
        assertEquals("30 seconds ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusSeconds(30), dateTime));
        assertEquals("in 1 sec", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusSeconds(1), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("in 30 secs", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusSeconds(30), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("1 sec ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusSeconds(1), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("30 secs ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusSeconds(30), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));

        assertEquals("in 1 minute", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusMinutes(1), dateTime));
        assertEquals("in 30 minutes", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusMinutes(30), dateTime));
        assertEquals("1 minute ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusMinutes(1), dateTime));
        assertEquals("30 minutes ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusMinutes(30), dateTime));
        assertEquals("in 1 min", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusMinutes(1), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("in 30 mins", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusMinutes(30), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("1 min ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusMinutes(1), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("30 mins ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusMinutes(30), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));

        assertEquals("in 1 hour", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusHours(1), dateTime));
        assertEquals("in 3 hours", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusHours(3), dateTime));
        assertEquals("1 hour ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusHours(1), dateTime));
        assertEquals("3 hours ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusHours(3), dateTime));
        assertEquals("in 1 hour", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusHours(1), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("in 3 hours", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusHours(3), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("1 hour ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusHours(1), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("3 hours ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusHours(3), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));

        assertEquals("tomorrow", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusDays(1), dateTime));
        assertEquals("in 3 days", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusDays(3), dateTime));
        assertEquals("yesterday", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusDays(1), dateTime));
        assertEquals("3 days ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusDays(3), dateTime));
        assertEquals("tomorrow", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusDays(1), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("in 3 days", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusDays(3), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("yesterday", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusDays(1), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));
        assertEquals("3 days ago", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusDays(3), dateTime,
            DateUtils.FORMAT_ABBREV_RELATIVE));

        assertEquals("Mar 11, 1988", DateUtils.getRelativeTimeSpanString(ctx, dateTime.plusWeeks(1), dateTime));
        assertEquals("Feb 26, 1988", DateUtils.getRelativeTimeSpanString(ctx, dateTime.minusWeeks(1), dateTime));

        // Test partial inputs
        LocalDate localDate = dateTime.toLocalDate();
        assertEquals("tomorrow", DateUtils.getRelativeTimeSpanString(ctx, localDate.plusDays(1), localDate));
        assertEquals("in 3 days", DateUtils.getRelativeTimeSpanString(ctx, localDate.plusDays(3), localDate));
        assertEquals("yesterday", DateUtils.getRelativeTimeSpanString(ctx, localDate.minusDays(1), localDate));
        assertEquals("3 days ago", DateUtils.getRelativeTimeSpanString(ctx, localDate.minusDays(3), localDate));

        LocalTime localTime = LocalTime.now();
        assertEquals("in 1 hour", DateUtils.getRelativeTimeSpanString(ctx, localTime.plusHours(1), localTime));
        assertEquals("in 3 hours", DateUtils.getRelativeTimeSpanString(ctx, localTime.plusHours(3), localTime));
        assertEquals("1 hour ago", DateUtils.getRelativeTimeSpanString(ctx, localTime.minusHours(1), localTime));
        assertEquals("3 hours ago", DateUtils.getRelativeTimeSpanString(ctx, localTime.minusHours(3), localTime));
    }
}
