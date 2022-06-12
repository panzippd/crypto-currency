package com.crypto.currency.common.utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author Panzi
 * @Description Utility class for common tools
 * @date 2022/4/29 17:16
 */
public class ExtUtils {

    public static final Pattern NUMREG = Pattern.compile("^[+|-]?\\d+(.?\\d)*$");

    /**
     * if V is Null,return 0 , or defaultValue
     *
     * @param v
     * @return
     */
    public static short getDefaultValue(Short v) {

        return v == null ? 0 : v.shortValue();
    }

    public static BigDecimal getDefaultValue(BigDecimal v) {

        return v == null ? BigDecimal.ZERO : v;
    }

    public static short getDefaultValue(Short v, short defaultValue) {

        return v == null ? defaultValue : v.shortValue();
    }

    public static long getDefaultValue(Long v) {

        return v == null ? 0 : v.longValue();
    }

    public static int getDefaultValue(Integer v, Integer defaultValue) {

        return v == null ? defaultValue.intValue() : v.intValue();
    }

    public static boolean getDefaultValue(Boolean v) {

        return v == null ? false : v.booleanValue();
    }

    public static BigDecimal parseBigDecimal(String value) {

        if (StringUtils.isBlank(value) || "null".equals(value)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    /**
     * get Null Bigdecimal the Default Value 0;
     *
     * @param a
     * @return
     */
    public static BigDecimal getNotNull(BigDecimal a) {

        if (null == a) {
            return BigDecimal.ZERO;
        }
        return a;
    }

    public static Long getLongNotNull(Long a) {

        if (null == a) {
            return 0L;
        }
        return a;
    }

    public static <T> T defaultIfNull(T t, T defaultValue) {

        if (t == null) {
            return defaultValue;
        }
        return t;
    }

    public static Integer parseInt(String str) {

        if (org.apache.commons.lang3.StringUtils.isBlank(str) || !NUMREG.matcher(str).matches()) {
            return null;
        }
        return Integer.valueOf(str);
    }

    public static boolean isNum(String str) {

        return (!org.apache.commons.lang3.StringUtils.isBlank(str) && NUMREG.matcher(str).matches());
    }

    public static boolean isSameSize(Collection a, Collection b) {

        if (null == a && null == b) {
            return true;
        } else if (null == a || null == b) {
            return false;
        }
        return a.size() == b.size();
    }

}
