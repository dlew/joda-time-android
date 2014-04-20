package net.danlew.android.joda.sample;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.danlew.android.joda.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrating the capabilities of joda-time-android.
 *
 * This is by no means a comprehensive demonstration of all the capabilities
 * of joda-time-android, but it gives you a flavor of what is possible.
 */
public class JodaSampleActivity extends Activity {

    private ViewGroup mContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_joda_sample);

        mContent = (ViewGroup) findViewById(R.id.content);

        sampleDateTime();
        sampleLocalDate();
        sampleFormatDateTime();
        sampleDateRange();
        sampleFormatElapsedTime();
        sampleFormatDuration();
        sampleIsToday();
        sampleGetRelativeTimeSpanString();
        sampleGetRelativeTimeSpanStringWithPreposition();
        sampleGetRelativeDateTimeString();
    }

    private void sampleDateTime() {
        List<String> text = new ArrayList<String>();
        DateTime now = DateTime.now();
        text.add("Now: " + now);
        text.add("Now + 30 minutes: " + now.plusMinutes(30));
        text.add("Now + 5 hours: " + now.plusHours(5));
        text.add("Now + 2 days: " + now.plusDays(2));
        addSample("DateTime", text);
    }

    private void sampleLocalDate() {
        List<String> text = new ArrayList<String>();
        LocalDate now = LocalDate.now();
        text.add("Now: " + now);
        text.add("Now + 2 days: " + now.plusDays(2));
        text.add("Now + 3 months: " + now.plusMonths(3));
        addSample("LocalDate", text);
    }

    // You can mix/match most flags for the desired output format
    private void sampleFormatDateTime() {
        List<String> text = new ArrayList<String>();
        DateTime now = DateTime.now();
        text.add("Show time: " + DateUtils.formatDateTime(this, now, DateUtils.FORMAT_SHOW_TIME));
        text.add("Show date: " + DateUtils.formatDateTime(this, now, DateUtils.FORMAT_SHOW_DATE));
        text.add("Numeric date: " + DateUtils.formatDateTime(this, now, DateUtils.FORMAT_NUMERIC_DATE));
        text.add("Show date (abbreviated): " + DateUtils.formatDateTime(this, now, DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_ABBREV_ALL));
        text.add("Show date and time: " + DateUtils.formatDateTime(this, now, DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_SHOW_TIME));
        text.add("Show date (force year): " + DateUtils.formatDateTime(this, now, DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_SHOW_YEAR));
        addSample("DateUtils.formatDateTime()", text);
    }

    // You can mix/match most flags for the desired output format
    private void sampleDateRange() {
        List<String> text = new ArrayList<String>();
        DateTime start = DateTime.now();
        DateTime end = start.plusMinutes(30).plusHours(2).plusDays(56);
        text.add("Range: " + DateUtils.formatDateRange(this, start, end, 0));
        text.add("Range (with year): " + DateUtils.formatDateRange(this, start, end, DateUtils.FORMAT_SHOW_YEAR));
        text.add("Range (abbreviated): " + DateUtils.formatDateRange(this, start, end, DateUtils.FORMAT_ABBREV_ALL));
        text.add("Range (with time): " + DateUtils.formatDateRange(this, start, end, DateUtils.FORMAT_SHOW_TIME));
        addSample("DateUtils.formatDateRange()", text);
    }

    private void sampleFormatElapsedTime() {
        List<String> text = new ArrayList<String>();
        text.add("25 seconds: " + DateUtils.formatElapsedTime(Duration.standardSeconds(25)));
        text.add("3 minutes: " + DateUtils.formatElapsedTime(Duration.standardMinutes(3)));
        text.add("3 minutes, 25 seconds: " + DateUtils.formatElapsedTime(
            Duration.standardMinutes(3).plus(Duration.standardSeconds(25))));
        text.add("3 hours: " + DateUtils.formatElapsedTime(Duration.standardHours(3)));
        text.add("3 hours, 3 minutes: " + DateUtils.formatElapsedTime(
            Duration.standardHours(3).plus(Duration.standardMinutes(3))));
        addSample("DateUtils.formatElapsedTime()", text);
    }

    private void sampleFormatDuration() {
        List<String> text = new ArrayList<String>();
        text.add("Seconds: " + DateUtils.formatDuration(this, Duration.standardSeconds(25)));
        text.add("Minutes: " + DateUtils.formatDuration(this, Duration.standardMinutes(5)));
        text.add("Hours: " + DateUtils.formatDuration(this, Duration.standardHours(3)));
        addSample("DateUtils.formatDuration()", text);
    }

    private void sampleIsToday() {
        List<String> text = new ArrayList<String>();
        LocalDate today = LocalDate.now();
        text.add("Today: " + DateUtils.isToday(today));
        text.add("Tomorrow: " + DateUtils.isToday(today.plusDays(1)));
        text.add("Yesterday: " + DateUtils.isToday(today.minusDays(1)));
        addSample("DateUtils.isToday()", text);
    }

    private void sampleGetRelativeTimeSpanString() {
        List<String> text = new ArrayList<String>();
        DateTime now = DateTime.now();
        text.add("Short future: " + DateUtils.getRelativeTimeSpanString(this, now.plusMinutes(25)));
        text.add("Medium future: " + DateUtils.getRelativeTimeSpanString(this, now.plusHours(5)));
        text.add("Long future: " + DateUtils.getRelativeTimeSpanString(this, now.plusDays(3)));
        text.add("Short past: " + DateUtils.getRelativeTimeSpanString(this, now.minusMinutes(25)));
        text.add("Medium past: " + DateUtils.getRelativeTimeSpanString(this, now.minusHours(5)));
        text.add("Long past: " + DateUtils.getRelativeTimeSpanString(this, now.minusDays(3)));
        addSample("DateUtils.getRelativeTimeSpanString()", text);
    }

    private void sampleGetRelativeTimeSpanStringWithPreposition() {
        List<String> text = new ArrayList<String>();
        DateTime now = DateTime.now();
        text.add("Short future: " + DateUtils.getRelativeTimeSpanString(this, now.plusMinutes(25), true));
        text.add("Medium future: " + DateUtils.getRelativeTimeSpanString(this, now.plusHours(5), true));
        text.add("Long future: " + DateUtils.getRelativeTimeSpanString(this, now.plusDays(3), true));
        text.add("Short past: " + DateUtils.getRelativeTimeSpanString(this, now.minusMinutes(25), true));
        text.add("Medium past: " + DateUtils.getRelativeTimeSpanString(this, now.minusHours(5), true));
        text.add("Long past: " + DateUtils.getRelativeTimeSpanString(this, now.minusDays(3), true));
        addSample("DateUtils.getRelativeTimeSpanString() (with preposition)", text);
    }

    private void sampleGetRelativeDateTimeString() {
        List<String> text = new ArrayList<String>();
        DateTime now = DateTime.now();
        text.add("Short future: " + DateUtils.getRelativeDateTimeString(this, now.plusMinutes(25), null, 0));
        text.add("Medium future: " + DateUtils.getRelativeDateTimeString(this, now.plusHours(5), null, 0));
        text.add("Long future: " + DateUtils.getRelativeDateTimeString(this, now.plusDays(3), null, 0));
        text.add("Short past: " + DateUtils.getRelativeDateTimeString(this, now.minusMinutes(25), null, 0));
        text.add("Medium past: " + DateUtils.getRelativeDateTimeString(this, now.minusHours(5), null, 0));
        text.add("Long past: " + DateUtils.getRelativeDateTimeString(this, now.minusDays(3), null, 0));
        addSample("DateUtils.getRelativeDateTimeString()", text);
    }

    private void addSample(CharSequence title, Iterable<String> text) {
        addSample(title, TextUtils.join("\n", text));
    }

    private void addSample(CharSequence title, CharSequence text) {
        View view = LayoutInflater.from(this).inflate(R.layout.include_sample, mContent, false);
        ((TextView) view.findViewById(R.id.title)).setText(title);
        ((TextView) view.findViewById(R.id.text)).setText(text);
        mContent.addView(view);
    }

}
