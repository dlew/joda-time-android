package net.danlew.android.joda;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Provides a method to initialize the {@link ResourceZoneInfoProvider}
 * and register a {@link TimeZoneChangedReceiver} to receive
 * android.intent.action.TIMEZONE_CHANGED broadcasts while the application
 * process is running.
 */
public final class JodaTimeAndroid {

    private JodaTimeAndroid() {
        // no instances
    }

    /**
     * Initializes ResourceZoneInfoProvider and registers an instance of
     * {@link TimeZoneChangedReceiver} to receive android.intent.action.TIMEZONE_CHANGED
     * broadcasts. */
    public static void init(Context context) {
        Context appContext = context.getApplicationContext();
        ResourceZoneInfoProvider.init(appContext);
        appContext.registerReceiver(new TimeZoneChangedReceiver(), new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));
    }
}
