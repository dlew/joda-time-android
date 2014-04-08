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
 * Factory for {@code StringConverter} providing support for Boolean object array
 * as a sequence of 'T', 'F' and '-' for null.
 * <p>
 * This is intended as a human readable format, not a compact format.
 * <p>
 * To use, simply register the instance with a {@code StringConvert} instance.
 * <p>
 * This class is immutable and thread-safe.
 * 
 * @since 1.5
 */
public final class BooleanObjectArrayStringConverterFactory implements StringConverterFactory {

    /**
     * Singleton instance.
     */
    public static final StringConverterFactory INSTANCE = new BooleanObjectArrayStringConverterFactory();

    /**
     * Restricted constructor.
     */
    private BooleanObjectArrayStringConverterFactory() {
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
        if (cls == Boolean[].class) {
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
    enum BooleanArrayStringConverter implements StringConverter<Boolean[]> {
        INSTANCE {
            @Override
            public String convertToString(Boolean[] array) {
                if (array.length == 0) {
                    return "";
                }
                StringBuilder buf = new StringBuilder(array.length);
                for (int i = 0; i < array.length; i++) {
                    buf.append(array[i] == null ? '-' : (array[i].booleanValue() ? 'T' : 'F'));
                }
                return buf.toString();
            }
            @Override
            public Boolean[] convertFromString(Class<? extends Boolean[]> cls, String str) {
                if (str.length() == 0) {
                    return EMPTY;
                }
                Boolean[] array = new Boolean[str.length()];
                for (int i = 0; i < array.length; i++) {
                    char ch = str.charAt(i);
                    if (ch == 'T') {
                        array[i] = Boolean.TRUE;
                    } else if (ch == 'F') {
                        array[i] = Boolean.FALSE;
                    } else if (ch == '-') {
                        array[i] = null;
                    } else {
                        throw new IllegalArgumentException("Invalid Boolean[] string, must consist only of 'T', 'F' and '-'");
                    }
                }
                return array;
            }
        };
        private static final Boolean[] EMPTY = new Boolean[0];
    }

}
