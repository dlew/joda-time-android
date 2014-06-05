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

    /** Whether the JodaTimeAndroid.init() method has been called. */
    private static boolean sInitCalled = false;

    private JodaTimeAndroid() {
        // no instances
    }

    /**
     * Initializes ResourceZoneInfoProvider and registers an instance of
     * {@link TimeZoneChangedReceiver} to receive android.intent.action.TIMEZONE_CHANGED
     * broadcasts. This method does nothing if previously called.
     */
    public static void init(Context context) {
        if (sInitCalled) {
            return;
        }

        sInitCalled = true;

        Context appContext = context.getApplicationContext();
        ResourceZoneInfoProvider.init(appContext);
        appContext.registerReceiver(new TimeZoneChangedReceiver(), new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));
    }

    /** Returns whether the init() method has been called. */
    protected static boolean hasInitBeenCalled() {
        return sInitCalled;
    }
}
