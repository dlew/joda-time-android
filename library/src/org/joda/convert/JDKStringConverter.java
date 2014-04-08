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
package org.joda.convert;

import android.util.Base64;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Conversion between JDK classes and a {@code String}.
 */
enum JDKStringConverter implements StringConverter<Object> {

    /**
     * String converter.
     */
    STRING(String.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return str;
        }
    },
    /**
     * CharSequence converter.
     */
    CHAR_SEQUENCE(CharSequence.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return str;
        }
    },
    /**
     * StringBuffer converter.
     */
    STRING_BUFFER(StringBuffer.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return new StringBuffer(str);
        }
    },
    /**
     * StringBuilder converter.
     */
    STRING_BUILDER(StringBuilder.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return new StringBuilder(str);
        }
    },
    /**
     * Long converter.
     */
    LONG(Long.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return new Long(str);
        }
    },

    /**
     * Integer converter.
     */
    INTEGER(Integer.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return new Integer(str);
        }
    },

    /**
     * Short converter.
     */
    SHORT (Short.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return new Short(str);
        }
    },

    /**
     * Byte converter.
     */
    BYTE(Byte.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return new Byte(str);
        }
    },
    /**
     * String converter.
     */
    BYTE_ARRAY(byte[].class) {
        @Override
        public String convertToString(Object object) {
            return Base64.encodeToString((byte[]) object, Base64.DEFAULT);
        }
        public Object convertFromString(Class<?> cls, String str) {
            return Base64.decode(str, Base64.DEFAULT);
        }
    },
    /**
     * Character converter.
     */
    CHARACTER(Character.class) {
        public Object convertFromString(Class<?> cls, String str) {
            if (str.length() != 1) {
                throw new IllegalArgumentException("Character value must be a string length 1");
            }
            return new Character(str.charAt(0));
        }
    },
    /**
     * String converter.
     */
    CHAR_ARRAY(char[].class) {
        @Override
        public String convertToString(Object object) {
            return new String((char[]) object);
        }
        public Object convertFromString(Class<?> cls, String str) {
            return str.toCharArray();
        }
    },
    /**
     * Boolean converter.
     */
    BOOLEAN(Boolean.class) {
        public Object convertFromString(Class<?> cls, String str) {
            if ("true".equalsIgnoreCase(str)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(str)) {
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Boolean value must be 'true' or 'false', case insensitive");
        }
    },
    /**
     * Double converter.
     */
    DOUBLE(Double.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return new Double(str);
        }
    },
    /**
     * Float converter.
     */
    FLOAT(Float.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return new Float(str);
        }
    },
    /**
     * BigInteger converter.
     */
    BIG_INTEGER(BigInteger.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return new BigInteger(str);
        }
    },
    /**
     * BigDecimal converter.
     */
    BIG_DECIMAL(BigDecimal.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return new BigDecimal(str);
        }
    },
    /**
     * AtomicLong converter.
     */
    ATOMIC_LONG(AtomicLong.class) {
        public Object convertFromString(Class<?> cls, String str) {
            long val = Long.parseLong(str);
            return new AtomicLong(val);
        }
    },
    /**
     * AtomicLong converter.
     */
    ATOMIC_INTEGER(AtomicInteger.class) {
        public Object convertFromString(Class<?> cls, String str) {
            int val = Integer.parseInt(str);
            return new AtomicInteger(val);
        }
    },
    /**
     * AtomicBoolean converter.
     */
    ATOMIC_BOOLEAN(AtomicBoolean.class) {
        public Object convertFromString(Class<?> cls, String str) {
            if ("true".equalsIgnoreCase(str)) {
                return new AtomicBoolean(true);
            }
            if ("false".equalsIgnoreCase(str)) {
                return new AtomicBoolean(false);
            }
            throw new IllegalArgumentException("Boolean value must be 'true' or 'false', case insensitive");
        }
    },
    /**
     * Locale converter.
     */
    LOCALE(Locale.class) {
        public Object convertFromString(Class<?> cls, String str) {
            String[] split = str.split("_", 3);
            switch (split.length) {
                case 1:
                    return new Locale(split[0]);
                case 2:
                    return new Locale(split[0], split[1]);
                case 3:
                    return new Locale(split[0], split[1], split[2]);
            }
            throw new IllegalArgumentException("Unable to parse Locale: " + str);
        }
    },
    /**
     * Class converter.
     */
    CLASS(Class.class) {
        @Override
        public String convertToString(Object object) {
            return ((Class<?>) object).getName();
        }
        public Object convertFromString(Class<?> cls, String str) {
            try {
                return RenameHandler.INSTANCE.lookupType(str);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Unable to create type: " + str, ex);
            }
        }
    },
    /**
     * Package converter.
     */
    PACKAGE(Package.class) {
        @Override
        public String convertToString(Object object) {
            return ((Package) object).getName();
        }
        public Object convertFromString(Class<?> cls, String str) {
            return Package.getPackage(str);
        }
    },
    /**
     * Currency converter.
     */
    CURRENCY(Currency.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return Currency.getInstance(str);
        }
    },
    /**
     * TimeZone converter.
     */
    TIME_ZONE(TimeZone.class) {
        @Override
        public String convertToString(Object object) {
            return ((TimeZone) object).getID();
        }
        public Object convertFromString(Class<?> cls, String str) {
            return TimeZone.getTimeZone(str);
        }
    },
    /**
     * UUID converter.
     */
    UUID(UUID.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return java.util.UUID.fromString(str);
        }
    },
    /**
     * URL converter.
     */
    URL(URL.class) {
        public Object convertFromString(Class<?> cls, String str) {
            try {
                return new URL(str);
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
    },
    /**
     * URI converter.
     */
    URI(URI.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return java.net.URI.create(str);
        }
    },
    /**
     * InetAddress converter.
     */
    INET_ADDRESS(InetAddress.class) {
        @Override
        public String convertToString(Object object) {
            return ((InetAddress) object).getHostAddress();
        }
        public Object convertFromString(Class<?> cls, String str) {
            try {
                return InetAddress.getByName(str);
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
        }
    },
    /**
     * File converter.
     */
    FILE(File.class) {
        public Object convertFromString(Class<?> cls, String str) {
            return new File(str);
        }
    },
    /**
     * Date converter.
     */
    DATE(Date.class) {
        @Override
        public String convertToString(Object object) {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String str = f.format(object);
            return str.substring(0, 26) + ":" + str.substring(26);
        }
        public Object convertFromString(Class<?> cls, String str) {
            if (str.length() != 29) {
                throw new IllegalArgumentException("Unable to parse date: " + str);
            }
            str = str.substring(0, 26) + str.substring(27);
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            try {
                return f.parseObject(str);
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        }
    },
    /**
     * Calendar converter.
     */
    CALENDAR(Calendar.class) {
        @Override
        public String convertToString(Object object) {
            if (object instanceof GregorianCalendar == false) {
                throw new RuntimeException("Unable to convert calendar as it is not a GregorianCalendar");
            }
            GregorianCalendar cal = (GregorianCalendar) object;
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            f.setCalendar(cal);
            String str = f.format(cal.getTime());
            return str.substring(0, 26) + ":" + str.substring(26) + "[" + cal.getTimeZone().getID() + "]";
        }
        public Object convertFromString(Class<?> cls, String str) {
            if (str.length() < 31 || str.charAt(26) != ':'
                    || str.charAt(29) != '[' || str.charAt(str.length() - 1) != ']') {
                throw new IllegalArgumentException("Unable to parse date: " + str);
            }
            TimeZone zone = TimeZone.getTimeZone(str.substring(30, str.length() - 1));
            str = str.substring(0, 26) + str.substring(27, 29);
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            GregorianCalendar cal = new GregorianCalendar(zone);
            cal.setTimeInMillis(0);
            f.setCalendar(cal);
            try {
                f.parseObject(str);
                return f.getCalendar();
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        }
    },
    /**
     * Enum converter.
     */
    ENUM(Enum.class) {
        @SuppressWarnings("rawtypes")
        public String convertToString(Object object) {
            return ((Enum) object).name();  // avoid toString() as that can be overridden
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Object convertFromString(Class cls, String str) {
            return RenameHandler.INSTANCE.lookupEnum(cls, str);
        }
    },
    ;

    /** The type. */
    private Class<?> type;

    /**
     * Creates an enum.
     * @param type  the type, not null
     */
    private JDKStringConverter(Class<?> type) {
        this.type = type;
    }

    /**
     * Gets the type of the converter.
     * @return the type, not null
     */
    Class<?> getType() {
        return type;
    }

    //-----------------------------------------------------------------------
    public String convertToString(Object object) {
        return object.toString();
    }

}
