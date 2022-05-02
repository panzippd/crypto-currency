package com.crypto.currency.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

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

    /**
     * 生成一个GUID 0 标准的UUID格式为：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx (8-4-4-4-12) 1
     * UUID格式为：xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     *
     * @return
     */
    public static String uuid(int type) {
        if (type == 1) {
            return UUID.randomUUID().toString().replace("-", StringUtils.EMPTY);
        }
        return UUID.randomUUID().toString();
    }

    public static String uuid() {

        return uuid(1);
    }

    public static LocalDateTime nowUTC() {

        return LocalDateTime.now(ZoneId.from(ZoneOffset.UTC));
    }

}
