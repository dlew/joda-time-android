package net.danlew.android.joda;

import android.content.Context;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;

/**
 * A replacement for android.text.format.DateUtils that uses Joda-Time classes.
 */
public class DateUtils {

    // The following FORMAT_* symbols are used for specifying the format of
    // dates and times in the formatDateRange method.
    public static final int FORMAT_SHOW_TIME = android.text.format.DateUtils.FORMAT_SHOW_TIME;
    public static final int FORMAT_SHOW_WEEKDAY = android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
    public static final int FORMAT_SHOW_YEAR = android.text.format.DateUtils.FORMAT_SHOW_YEAR;
    public static final int FORMAT_NO_YEAR = android.text.format.DateUtils.FORMAT_NO_YEAR;
    public static final int FORMAT_SHOW_DATE = android.text.format.DateUtils.FORMAT_SHOW_DATE;
    public static final int FORMAT_NO_MONTH_DAY = android.text.format.DateUtils.FORMAT_NO_MONTH_DAY;
    public static final int FORMAT_NO_NOON = android.text.format.DateUtils.FORMAT_NO_NOON;
    public static final int FORMAT_NO_MIDNIGHT = android.text.format.DateUtils.FORMAT_NO_MIDNIGHT;
    public static final int FORMAT_ABBREV_TIME = android.text.format.DateUtils.FORMAT_ABBREV_TIME;
    public static final int FORMAT_ABBREV_WEEKDAY = android.text.format.DateUtils.FORMAT_ABBREV_WEEKDAY;
    public static final int FORMAT_ABBREV_MONTH = android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
    public static final int FORMAT_NUMERIC_DATE = android.text.format.DateUtils.FORMAT_NUMERIC_DATE;
    public static final int FORMAT_ABBREV_RELATIVE = android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE;
    public static final int FORMAT_ABBREV_ALL = android.text.format.DateUtils.FORMAT_ABBREV_ALL;


    /**
     * We don't want consumers of DateUtils to use this, but we do need it internally to calibrate
     * times to UTC for formatting purposes.
     */
    private static final int FORMAT_UTC = android.text.format.DateUtils.FORMAT_UTC;

    private static final DateTime EPOCH = new DateTime(0, DateTimeZone.UTC);

    /**
     * Formats a date or a time according to the local conventions.
     *
     * Since ReadablePartials don't support all fields, we fill in any blanks
     * needed for formatting by using the epoch (1970-01-01T00:00:00Z).
     *
     * See {@link android.text.format.DateUtils#formatDateTime} for full docs.
     *
     * @param context the context is required only if the time is shown
     * @param time a point in time
     * @param flags a bit mask of formatting options
     * @return a string containing the formatted date/time.
     */
    public static String formatDateTime(Context context, ReadablePartial time, int flags) {
        return android.text.format.DateUtils.formatDateTime(context, toMillis(time), flags | FORMAT_UTC);
    }

    /**
     * Formats a date or a time according to the local conventions.
     *
     * See {@link android.text.format.DateUtils#formatDateTime} for full docs.
     *
     * @param context the context is required only if the time is shown
     * @param time a point in time
     * @param flags a bit mask of formatting options
     * @return a string containing the formatted date/time.
     */
    public static String formatDateTime(Context context, ReadableInstant time, int flags) {
        return android.text.format.DateUtils.formatDateTime(context, toMillis(time), flags | FORMAT_UTC);
    }

    private static long toMillis(ReadablePartial time) {
        return time.toDateTime(EPOCH).getMillis();
    }

    private static long toMillis(ReadableInstant time) {
        DateTime dateTime = time instanceof DateTime ? (DateTime) time : new DateTime(time);
        DateTime utcDateTime = dateTime.withZoneRetainFields(DateTimeZone.UTC);
        return utcDateTime.getMillis();
    }

    /**
     * Formats an elapsed time in the form "MM:SS" or "H:MM:SS"
     * for display on the call-in-progress screen.
     *
     * See {@link android.text.format.DateUtils#formatElapsedTime} for full docs.
     *
     * @param elapsedDuration the elapsed duration
     */
    public static String formatElapsedTime(ReadableDuration elapsedDuration) {
        return formatElapsedTime(null, elapsedDuration);
    }

    /**
     * Formats an elapsed time in a format like "MM:SS" or "H:MM:SS" (using a form
     * suited to the current locale), similar to that used on the call-in-progress
     * screen.
     *
     * See {@link android.text.format.DateUtils#formatElapsedTime} for full docs.
     *
     * @param recycle {@link StringBuilder} to recycle, or null to use a temporary one.
     * @param elapsedDuration the elapsed duration
     */
    public static String formatElapsedTime(StringBuilder recycle, ReadableDuration elapsedDuration) {
        return android.text.format.DateUtils.formatElapsedTime(recycle,
            elapsedDuration.toDuration().toStandardSeconds().getSeconds());
    }

    /**
     * See {@link android.text.format.DateUtils#isToday} for full docs.
     *
     * @return true if the supplied when is today else false
     */
    public static boolean isToday(ReadablePartial time) {
        if (!time.isSupported(DateTimeFieldType.dayOfMonth())
            || !time.isSupported(DateTimeFieldType.monthOfYear())
            || !time.isSupported(DateTimeFieldType.year())) {
            throw new IllegalArgumentException("isToday() must be passed a ReadablePartial that supports day of " +
                "month, month of year and year.");
        }

        LocalDate localDate = time instanceof LocalDate ? (LocalDate) time : new LocalDate(time);
        return LocalDate.now().compareTo(localDate) == 0;
    }

    /**
     * See {@link android.text.format.DateUtils#isToday} for full docs.
     *
     * @return true if the supplied when is today else false
     */
    public static boolean isToday(ReadableInstant time) {
        return LocalDate.now().compareTo(new LocalDate(time)) == 0;
    }
}
