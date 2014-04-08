/*
 *  Copyright 2010-present Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.convert.factory;

import org.joda.convert.StringConverter;
import org.joda.convert.StringConverterFactory;

/**
 * Factory for {@code StringConverter} providing support for primitive boolean array
 * as a sequence of 'T' and 'F'.
 * <p>
 * This is intended as a human readable format, not a compact format.
 * <p>
 * To use, simply register the instance with a {@code StringConvert} instance.
 * <p>
 * This class is immutable and thread-safe.
 * 
 * @since 1.5
 */
public final class BooleanArrayStringConverterFactory implements StringConverterFactory {

    /**
     * Singleton instance.
     */
    public static final StringConverterFactory INSTANCE = new BooleanArrayStringConverterFactory();

    /**
     * Restricted constructor.
     */
    private BooleanArrayStringConverterFactory() {
    }

    //-----------------------------------------------------------------------
    /**
     * Finds a converter by type.
     * 
     * @param cls  the type to lookup, not null
     * @return the converter, null if not found
     * @throws RuntimeException (or subclass) if source code is invalid
     */
    public StringConverter<?> findConverter(Class<?> cls) {
        if (cls == boolean[].class) {
            return BooleanArrayStringConverter.INSTANCE;
        }
        return null;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    //-----------------------------------------------------------------------
    enum BooleanArrayStringConverter implements StringConverter<boolean[]> {
        INSTANCE {
            @Override
            public String convertToString(boolean[] array) {
                if (array.length == 0) {
                    return "";
                }
                StringBuilder buf = new StringBuilder(array.length);
                for (int i = 0; i < array.length; i++) {
                    buf.append(array[i] ? 'T' : 'F');
                }
                return buf.toString();
            }
            @Override
            public boolean[] convertFromString(Class<? extends boolean[]> cls, String str) {
                if (str.length() == 0) {
                    return EMPTY;
                }
                boolean[] array = new boolean[str.length()];
                for (int i = 0; i < array.length; i++) {
                    char ch = str.charAt(i);
                    if (ch == 'T') {
                        array[i] = true;
                    } else if (ch == 'F') {
                        array[i] = false;
                    } else {
                        throw new IllegalArgumentException("Invalid boolean[] string, must consist only of 'T' and 'F'");
                    }
                }
                return array;
            }
        };
        private static final boolean[] EMPTY = new boolean[0];
    }

}
