package net.danlew.android.joda;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JodaTimeInitializer implements Initializer<Object> {

    @NonNull
    @Override
    public Object create(@NonNull Context context) {
        try {
            DateTimeZone.setProvider(new ResourceZoneInfoProvider(context));
        } catch (IOException e) {
            throw new RuntimeException("Could not read ZoneInfoMap. You are probably using Proguard wrong.", e);
        }

        context.getApplicationContext()
                .registerReceiver(new TimeZoneChangedReceiver(), new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));

        return new Object();
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }

}
