package net.danlew.android.joda;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utilities for translating the normal output of ZoneInfoCompiler/
 * ZoneInfoProvider into the raw resources for Android
 */
public class ResUtils {

    private static final String TZDATA_PREFIX = "__tzdata_";

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

}
