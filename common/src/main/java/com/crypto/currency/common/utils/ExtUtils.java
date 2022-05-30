package com.crypto.currency.common.utils;

import java.math.BigDecimal;

/**
 * @author Panzi
 * @Description Utility class for common tools
 * @date 2022/4/29 17:16
 */
public class ExtUtils {

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

}
