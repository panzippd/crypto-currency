package com.crypto.currency.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Panzi
 * @Description Utils for any object related operations
 * @date 2022/5/3 10:42
 */
public class ObjectUtils {
    /**
     * Checks if the objects given are all null
     *
     * @param objects the objects to check
     * @return true if all objects are null; false if any of the object is not null
     */
    public static boolean isAllNull(Object... objects) {
        for (Object object : objects) {
            if (object != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any of the objects is null.
     *
     * @param objects the objects to check
     * @return true if any of the objects is null; false if all objects are not null.
     */
    public static boolean isAnyNull(Object... objects) {
        for (Object object : objects) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calls the {@link Object#toString()} method for the passed in object. If the object is null then
     * an empty string will be returned.
     *
     * @param o the object passed in
     * @return the result of calling o.toString() or "" if the object is null
     */
    public static String toString(Object o) {
        return o == null ? "" : o.toString();
    }

    /**
     * Returns the object itself or the default value if it's null
     *
     * @param t            the object
     * @param defaultValue the default value
     * @param <T>          the type of the object
     * @return if the object is not null then the object itself; otherwise the default value
     */
    public static <T> T defaultIfNull(T t, T defaultValue) {
        if (t == null) {
            return defaultValue;
        }
        return t;
    }

    /**
     * convert n to BigDecimal
     *
     * @param n the n for conversion
     * @return
     */
    public static BigDecimal toBigDecimal(Object n) {

        if (n instanceof BigDecimal) {
            return (BigDecimal)n;
        } else if (n instanceof Number || n instanceof CharSequence) {
            return new BigDecimal(Objects.toString(n));
        }
        return null;
    }

    /**
     * convert n to double
     *
     * @param n the n for conversion
     * @return
     */
    public static Double toDouble(Object n) {

        if (n instanceof Double) {
            return (Double)n;
        } else if (n instanceof Number || n instanceof CharSequence) {
            return Double.valueOf(Objects.toString(n));
        }
        return null;
    }

    /**
     * convert n to int
     *
     * @param n the n for conversion
     * @return
     */
    public static Integer toInt(Object n) {

        if (n instanceof Integer) {
            return (Integer)n;
        } else if (n instanceof Number || n instanceof CharSequence) {
            return Integer.valueOf(Objects.toString(n));
        }
        return null;
    }

    /**
     * convert n to long
     *
     * @param n the n for conversion
     * @return
     */
    public static Long toLong(Object n) {

        if (n instanceof Long) {
            return (Long)n;
        } else if (n instanceof Number || n instanceof CharSequence) {
            return Long.valueOf(Objects.toString(n));
        }
        return null;
    }

    /**
     * convert n to bool
     *
     * @param n the n for conversion
     * @return
     */
    public static Boolean toBool(Object n) {

        if (n instanceof Boolean) {
            return (Boolean)n;
            //  ***CMC Node service is compatible with special transformations.
        } else if (n instanceof Number) {
            return Objects.equals(n, 1);
        } else if (n instanceof CharSequence) {
            return StringUtils.equalsIgnoreCase(n.toString(), "true");
        }
        return null;
    }

    /**
     * if t is true ,the object will be created.
     *
     * @return
     */
    public static <T> T computeIfAbsent(boolean t, Supplier<T> supplier) {

        if (t && supplier != null) {
            return supplier.get();
        }
        return null;
    }
}
