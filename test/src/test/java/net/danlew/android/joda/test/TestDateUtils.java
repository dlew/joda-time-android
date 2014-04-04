package net.danlew.android.joda.test;

import android.test.InstrumentationTestCase;
import net.danlew.android.joda.DateUtils;
import org.joda.time.Duration;

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

}
