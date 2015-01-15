package net.danlew.android.joda;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import org.joda.time.DateTimeZone;

import java.io.IOException;

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
        throw new AssertionError();
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

        try {
            DateTimeZone.setProvider(new ResourceZoneInfoProvider(context));
        }
        catch (IOException e) {
            throw new RuntimeException("Could not read ZoneInfoMap");
        }

        context.getApplicationContext()
            .registerReceiver(new TimeZoneChangedReceiver(), new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));
    }
}
