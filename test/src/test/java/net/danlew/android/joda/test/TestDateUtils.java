package net.danlew.android.joda.test;

import android.test.InstrumentationTestCase;
import net.danlew.android.joda.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.MonthDay;
import org.joda.time.YearMonth;

import java.util.Calendar;

/**
 * Test our implementation of DateUtils methods against the actual Android
 * DateUtils implementation.
 */
public class TestDateUtils extends InstrumentationTestCase {

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
}
