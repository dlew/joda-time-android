package net.danlew.android.joda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.joda.time.DateTimeZone;

import java.util.TimeZone;

/**
 * Listens for android.intent.action.TIMEZONE_CHANGED and adjusts
 * default DateTimeZone as necessary.
 */
public class TimeZoneChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String tzId = intent.getStringExtra("time-zone");

        try {
            DateTimeZone newDefault = DateTimeZone.forTimeZone(TimeZone.getDefault());
            DateTimeZone.setDefault(newDefault);
            Log.d("joda-time-android", "TIMEZONE_CHANGED received, changed default timezone to \"" + tzId + "\"");
        }
        catch (IllegalArgumentException e) {
            Log.e("joda-time-android", "Could not recognize timezone id \"" + tzId + "\"", e);
        }
    }

}
