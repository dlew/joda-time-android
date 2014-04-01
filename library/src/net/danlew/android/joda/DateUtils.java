package net.danlew.android.joda;

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

}
