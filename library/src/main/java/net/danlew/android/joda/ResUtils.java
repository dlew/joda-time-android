package net.danlew.android.joda;

import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilities for translating the normal output of ZoneInfoCompiler/
 * ZoneInfoProvider into the raw resources for Android
 */
public class ResUtils {

    private static final String TZDATA_PREFIX = "joda_";

    /**
     * Converts any path into something that can be placed in an Android directory.
     *
     * Traverses any subdirectories and flattens it all into a single filename.  Also
     * gets rid of commonly seen illegal characters in tz identifiers, and lower cases
     * the entire thing.
     *
     * @param path the path to convert
     * @return a flat path with no directories (and lower-cased)
     */
    private static String convertPathToResource(String path) {
        File file = new File(path);
        List<String> parts = new ArrayList<String>();
        do {
            parts.add(file.getName());
            file = file.getParentFile();
        }
        while (file != null);

        StringBuffer sb = new StringBuffer();
        int size = parts.size();
        for (int a = size - 1; a >= 0; a--) {
            if (sb.length() > 0) {
                sb.append("_");
            }
            sb.append(parts.get(a));
        }

        // TODO: Better regex replacement
        return sb.toString().replace('-', '_').replace("+", "plus").toLowerCase(Locale.US);
    }

    /**
     * Returns a resource name equivalent for a TZ ID
     *
     * @param tzFile the ID of the TZ, or a special filename (e.g. "ZoneInfoMap")
     * @return the resource name
     */
    public static String getTzResource(String tzFile) {
        return TZDATA_PREFIX + convertPathToResource(tzFile);
    }

    /**
     * @return the resource name for the zone info map
     */
    public static String getZoneInfoMapResource() {
        return TZDATA_PREFIX + convertPathToResource("ZoneInfoMap");
    }

    /** Cache of resources ids, for speed */
    private static Map<Class<?>, Map<String, Integer>> sIdentifierCache = new ConcurrentHashMap<Class<?>, Map<String, Integer>>();

    /**
     * Retrieves a resource id dynamically, via reflection.  It's much faster
     * than Resources.getIdentifier(), however it only allows you to get
     * identifiers from your own package. 
     * 
     * Note that this method is still slower than retrieving resources
     * directly (e.g., R.drawable.MyResource) - it should only be used
     * when dynamically retrieving ids.
     * 
     * Originally sourced from https://github.com/dlew/android-utils/
     * 
     * @param type the type of resource (e.g. R.drawable.class, R.layout.class, etc.)
     * @param name the name of the resource
     * @return the resource id, or 0 if not found
     */
    public static int getIdentifier(Class<?> type, String name) {
        // See if the cache already contains this identifier
        Map<String, Integer> typeCache;
        if (!sIdentifierCache.containsKey(type)) {
            typeCache = new ConcurrentHashMap<String, Integer>();
            sIdentifierCache.put(type, typeCache);
        }
        else {
            typeCache = sIdentifierCache.get(type);
        }

        if (typeCache.containsKey(name)) {
            return typeCache.get(name);
        }

        // Retrieve the identifier
        try {
            Field field = type.getField(name);
            int resId = field.getInt(null);

            if (resId != 0) {
                typeCache.put(name, resId);
            }

            return resId;
        }
        catch (Exception e) {
            Log.e("JodaTimeAndroid", "Failed to retrieve identifier: type=" + type + " name=" + name, e);
            return 0;
        }
    }

}
