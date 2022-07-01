package com.crypto.currency.common.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author Panzi
 * @Description Utility class for common tools
 * @date 2022/4/29 17:16
 */
public class ExtUtils {

    public static final Pattern NUMREG = Pattern.compile("^[+|-]?\\d+(.?\\d)*$");

    /**
     * Define frequently used time formatting
     */
    public enum LocalDateFormatEnum {

        YYYY_MM_DD(DateTimeFormatter.ofPattern("yyyy-MM-dd")),

        YYYY_MM_DD_HH_MM_SS(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),

        YYYYMMDD(DateTimeFormatter.ofPattern("yyyyMMdd")),

        YYYYMMDDHHMMSS(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        final DateTimeFormatter dateFormatter;

        LocalDateFormatEnum(DateTimeFormatter dateFormatter) {
            this.dateFormatter = dateFormatter;
        }

        public DateTimeFormatter getDateFormatter() {
            return dateFormatter;
        }
    }

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

    public static LocalDateTime nowUTC() {
        return LocalDateTime.now(ZoneId.from(ZoneOffset.UTC));
    }

    /**
     * @param index            Positive numbers are subtracted months, negative numbers are added months
     * @param simpleDateFormat Output time format, year format must use yyyy
     * @return
     */
    public static String minusMonths(int index, String simpleDateFormat) {
        DateTimeFormatter fmt;
        if (StringUtils.isEmpty(simpleDateFormat)) {
            fmt = LocalDateFormatEnum.YYYY_MM_DD_HH_MM_SS.getDateFormatter();   //default
        } else {
            fmt = DateTimeFormatter.ofPattern(simpleDateFormat);
        }
        LocalDateTime date = ExtUtils.nowUTC().minusMonths(index);  //by UTC time
        String monthTime = fmt.format(date);
        return monthTime;
    }

    /**
     * Splicing characters
     *
     * @param splitChar
     * @param objects
     * @return
     */
    public static String join(String splitChar, Object... objects) {

        if (ArrayUtils.isEmpty(objects)) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
        return org.apache.commons.lang3.StringUtils.join(objects, splitChar);
    }

    /**
     * Splicing characters
     *
     * @param splitChar
     * @param objects
     * @return
     */
    public static String join(char splitChar, Object... objects) {

        if (ArrayUtils.isEmpty(objects)) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
        return org.apache.commons.lang3.StringUtils.join(objects, splitChar);
    }

    public static void main(String[] args) {
        String startDateStr = ExtUtils.minusMonths(2, "yyyy-MM");
        String endDateStr = ExtUtils.minusMonths(1, "yyyy-MM");
        System.out.println(startDateStr);
        System.out.println(endDateStr);
    }

    /**
     * Map to  List
     *
     * @param map
     * @param fun
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(Map map, Function<Map.Entry, T> fun) {

        if (CollectionUtils.isEmpty(map) || fun == null) {
            return List.of();
        }

        List<T> data = Lists.newArrayListWithCapacity(map.size());
        for (Object kv : map.entrySet()) {
            Map.Entry entry = (Map.Entry)kv;
            data.add(fun.apply(entry));
        }
        return data;
    }

    /**
     * @param data
     * @return
     */
    public static Set<String> toSet(String data) {

        if (org.apache.commons.lang3.StringUtils.isBlank(data)) {
            return Set.of();
        }
        String[] dataArray = org.apache.commons.lang3.StringUtils.split(data, '|');
        Set<String> dataSet = Sets.newHashSetWithExpectedSize(dataArray.length);
        for (String d : dataArray) {
            dataSet.add(d);
        }
        return dataSet;
    }
}
