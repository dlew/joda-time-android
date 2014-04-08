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

import java.util.Arrays;
import java.util.regex.Pattern;

import org.joda.convert.StringConverter;
import org.joda.convert.StringConverterFactory;

/**
 * Factory for {@code StringConverter} providing support for Character object arrays
 * as a string, using backslash as an escape.
 * <p>
 * Double backslash is a backslash.
 * One backslash followed by a dash is null.
 * <p>
 * To use, simply register the instance with a {@code StringConvert} instance.
 * <p>
 * This class is immutable and thread-safe.
 * 
 * @since 1.5
 */
public final class CharObjectArrayStringConverterFactory implements StringConverterFactory {

    /**
     * Singleton instance.
     */
    public static final StringConverterFactory INSTANCE = new CharObjectArrayStringConverterFactory();
    /**
     * Delimiter to find.
     */
    static final Pattern DELIMITER = Pattern.compile("[,]");

    /**
     * Restricted constructor.
     */
    private CharObjectArrayStringConverterFactory() {
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
        if (cls == Character[].class) {
            return CharecterArrayStringConverter.INSTANCE;
        }
        return null;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    //-----------------------------------------------------------------------
    enum CharecterArrayStringConverter implements StringConverter<Character[]> {
        INSTANCE {
            @Override
            public String convertToString(Character[] array) {
                if (array.length == 0) {
                    return "";
                }
                StringBuilder buf = new StringBuilder(array.length * 8);
                for (int i = 0; i < array.length; i++) {
                    if (array[i] == null) {
                        buf.append("\\-");
                    } else {
                        char ch = array[i].charValue();
                        if (ch == '\\') {
                            buf.append("\\\\");
                        } else {
                            buf.append(ch);
                        }
                    }
                }
                return buf.toString();
            }
            @Override
            public Character[] convertFromString(Class<? extends Character[]> cls, String str) {
                if (str.length() == 0) {
                    return EMPTY;
                }
                Character[] array = new Character[str.length()];
                int arrayPos = 0;
                int pos;
                while ((pos = str.indexOf('\\')) >= 0) {
                    for (int i = 0; i < pos; i++) {
                        array[arrayPos++] = str.charAt(i);
                    }
                    if (str.charAt(pos + 1) == '\\') {
                        array[arrayPos++] = '\\';
                    } else if (str.charAt(pos + 1) == '-') {
                        array[arrayPos++] = null;
                    } else {
                        throw new IllegalArgumentException("Invalid Character[] string, incorrect escape");
                    }
                    str = str.substring(pos + 2);
                }
                for (int i = 0; i < str.length(); i++) {
                    array[arrayPos++] = str.charAt(i);
                }
                return Arrays.copyOf(array, arrayPos);
            }
        };
        private static final Character[] EMPTY = new Character[0];
    }

}
