package net.danlew.android.joda;

import android.content.Context;

/**
 * Provides metadata about joda-time-android.
 */
public final class JodaTimeAndroid {

    public static String TZ_DATA_VERSION = "2021a";

    private JodaTimeAndroid() {
        // no instances
        throw new AssertionError();
    }

    /**
     * @deprecated This library self-initializes itself now; calling this method does nothing
     */
    @Deprecated
    public static void init(Context context) {
    }
}
