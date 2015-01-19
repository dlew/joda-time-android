package net.danlew.android.joda;

import android.content.Context;
import android.util.Log;
import org.joda.time.DateTimeZone;
import org.joda.time.tz.DateTimeZoneBuilder;
import org.joda.time.tz.Provider;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A version of ZoneInfoProvider that loads its data from
 * Android resources.
 *
 * In order to give it access to Resources, you must call
 * JodaTimeAndroid.init() before starting to use Joda-Time.
 */
public class ResourceZoneInfoProvider implements Provider {

    /** The application context, used for retrieving resources */
    private Context mAppContext;

    /** Maps ids to strings or SoftReferences to DateTimeZones. */
    private final Map<String, Object> iZoneInfoMap;

    public ResourceZoneInfoProvider(Context context) throws IOException {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null");
        }

        mAppContext = context.getApplicationContext();
        iZoneInfoMap = loadZoneInfoMap(openResource("ZoneInfoMap"));
    }

    //-----------------------------------------------------------------------
    /**
     * If an error is thrown while loading zone data, the exception is logged
     * to system error and null is returned for this and all future requests.
     *
     * @param id  the id to load
     * @return the loaded zone
     */
    public DateTimeZone getZone(String id) {
        if (id == null) {
            return null;
        }

        Object obj = iZoneInfoMap.get(id);
        if (obj == null) {
            return null;
        }

        if (id.equals(obj)) {
            // Load zone data for the first time.
            return loadZoneData(id);
        }

        if (obj instanceof SoftReference<?>) {
            @SuppressWarnings("unchecked")
            SoftReference<DateTimeZone> ref = (SoftReference<DateTimeZone>) obj;
            DateTimeZone tz = ref.get();
            if (tz != null) {
                return tz;
            }
            // Reference cleared; load data again.
            return loadZoneData(id);
        }

        // If this point is reached, mapping must link to another.
        return getZone((String) obj);
    }

    /**
     * Gets a list of all the available zone ids.
     *
     * @return the zone ids
     */
    public Set<String> getAvailableIDs() {
        // Return a copy of the keys rather than an umodifiable collection.
        // This prevents ConcurrentModificationExceptions from being thrown by
        // some JVMs if zones are opened while this set is iterated over.
        return new TreeSet<String>(iZoneInfoMap.keySet());
    }

    /**
     * Called if an exception is thrown from getZone while loading zone data.
     *
     * @param ex  the exception
     */
    protected void uncaughtException(Exception ex) {
        ex.printStackTrace();
    }

    /**
     * Opens a resource from file or classpath.
     *
     * @param name  the name to open
     * @return the input stream
     * @throws IOException if an error occurs
     */
    private InputStream openResource(String name) throws IOException {
        if (mAppContext == null) {
            throw new RuntimeException("Need to call JodaTimeAndroid.init() before using joda-time-android");
        }

        String resName = ResUtils.getTzResource(name);
        int resId = ResUtils.getIdentifier(R.raw.class, resName);

        if (resId == 0) {
            throw new IOException("Resource not found: \"" + name + "\" (resName: \"" + resName + "\"");
        }

        InputStream in = mAppContext.getResources().openRawResource(resId);

        return in;
    }

    /**
     * Loads the time zone data for one id.
     *
     * @param id  the id to load
     * @return the zone
     */
    private DateTimeZone loadZoneData(String id) {
        InputStream in = null;
        try {
            in = openResource(id);
            DateTimeZone tz = DateTimeZoneBuilder.readFrom(in, id);
            iZoneInfoMap.put(id, new SoftReference<DateTimeZone>(tz));
            return tz;
        }
        catch (IOException ex) {
            uncaughtException(ex);
            iZoneInfoMap.remove(id);
            return null;
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException ex) {
            }
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Loads the zone info map.
     *
     * @param in  the input stream
     * @return the map
     */
    private static Map<String, Object> loadZoneInfoMap(InputStream in) throws IOException {
        Map<String, Object> map = new ConcurrentHashMap<String, Object>();
        DataInputStream din = new DataInputStream(in);
        try {
            readZoneInfoMap(din, map);
        }
        finally {
            try {
                din.close();
            }
            catch (IOException ex) {
            }
        }
        map.put("UTC", new SoftReference<DateTimeZone>(DateTimeZone.UTC));
        return map;
    }

    /**
     * Reads the zone info map from file.
     *
     * @param din  the input stream
     * @param zimap  gets filled with string id to string id mappings
     */
    private static void readZoneInfoMap(DataInputStream din, Map<String, Object> zimap) throws IOException {
        // Read the string pool.
        int size = din.readUnsignedShort();
        String[] pool = new String[size];
        for (int i = 0; i < size; i++) {
            pool[i] = din.readUTF().intern();
        }

        // Read the mappings.
        size = din.readUnsignedShort();
        for (int i = 0; i < size; i++) {
            try {
                zimap.put(pool[din.readUnsignedShort()], pool[din.readUnsignedShort()]);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new IOException("Corrupt zone info map");
            }
        }
    }

}
