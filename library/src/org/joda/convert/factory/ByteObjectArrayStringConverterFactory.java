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
 * Factory for {@code StringConverter} providing support for Byte object array
 * as a sequence of two letter hex codes for each byte plus '--' for null.
 * <p>
 * This is intended as a human readable format, not a compact format.
 * <p>
 * To use, simply register the instance with a {@code StringConvert} instance.
 * <p>
 * This class is immutable and thread-safe.
 * 
 * @since 1.5
 */
public final class ByteObjectArrayStringConverterFactory implements StringConverterFactory {

    /**
     * Singleton instance.
     */
    public static final StringConverterFactory INSTANCE = new ByteObjectArrayStringConverterFactory();

    /**
     * Restricted constructor.
     */
    private ByteObjectArrayStringConverterFactory() {
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
        if (cls == Byte[].class) {
            return ByteArrayStringConverter.INSTANCE;
        }
        return null;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    //-----------------------------------------------------------------------
    enum ByteArrayStringConverter implements StringConverter<Byte[]> {
        INSTANCE {
            @Override
            public String convertToString(Byte[] array) {
                if (array.length == 0) {
                    return "";
                }
                StringBuilder buf = new StringBuilder(array.length);
                for (int i = 0; i < array.length; i++) {
                    if (array[i] == null) {
                        buf.append('-').append('-');
                    } else {
                        int b = array[i].byteValue();
                        buf.append(HEX.charAt((b & 0xF0) >>> 4)).append(HEX.charAt(b & 0x0F));
                    }
                }
                return buf.toString();
            }
            @Override
            public Byte[] convertFromString(Class<? extends Byte[]> cls, String str) {
                if (str.length() == 0) {
                    return EMPTY;
                }
                if (str.length() % 2 == 1) {
                    throw new IllegalArgumentException("Invalid Byte[] string");
                }
                Byte[] array = new Byte[str.length() / 2];
                for (int i = 0; i < array.length; i++) {
                    String in = str.substring(i * 2, i * 2 + 2);
                    if (in.equals("--")) {
                        array[i] = null;
                    } else {
                        array[i] = (byte) Integer.parseInt(in, 16);
                    }
                }
                return array;
            }
        };
        private static final Byte[] EMPTY = new Byte[0];
        private static final String HEX = "0123456789ABCDEF";
    }

}
