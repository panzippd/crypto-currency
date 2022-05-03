package com.crypto.currency.common.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Panzi
 * @Description Utility class for date and time operations
 * @date 2022/5/3 10:39
 */
public class DateTimeUtils {

    public static final int SECONDS_OF_A_DAY = 86400;
    public static final int SECONDS_OF_A_HOUR = 3600;
    public static final ZoneId UTC = ZoneId.of("+00:00");
    public static final DateTimeFormatter NOW_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final String[] CMC_IOS8601_PATTERN =
        {"yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd", "yyyyMMdd"};

    /**
     * Gets the date object any days ago of the start time.
     *
     * @param startTime the start time
     * @param daysAgo   number of days to roll back
     * @return the new date object
     */
    public static Date getDaysAgo(Date startTime, int daysAgo) {
        Instant instant = startTime.toInstant().minus(daysAgo, ChronoUnit.DAYS);
        return Date.from(instant);
    }

    /**
     * Gets the date object any hours ago of the start time.
     *
     * @param startTime the start time
     * @param hoursAgo  number of hours to roll back
     * @return the new date object
     */
    public static Date getHoursAgo(Date startTime, int hoursAgo) {
        Instant instant = startTime.toInstant().minus(hoursAgo, ChronoUnit.HOURS);
        return Date.from(instant);
    }

    /**
     * Gets the date object any minutes ago of the start time.
     *
     * @param startTime  the start time
     * @param minutesAgo number of seconds to roll back
     * @return the new date object
     */
    public static Date getMinutesAgo(Date startTime, long minutesAgo) {
        Instant instant = startTime.toInstant().minus(minutesAgo, ChronoUnit.MINUTES);
        return Date.from(instant);
    }

    /**
     * Gets the date object any seconds ago of the start time.
     *
     * @param startTime  the start time
     * @param secondsAgo number of seconds to roll back
     * @return the new date object
     */
    public static Date getSecondsAgo(Date startTime, long secondsAgo) {
        Instant instant = startTime.toInstant().minus(secondsAgo, ChronoUnit.SECONDS);
        return Date.from(instant);
    }

    /**
     * Gets number of seconds in days given
     *
     * @param numberOfDays the number of days
     * @return the seconds
     */
    public static int getSecondsInDays(int numberOfDays) {
        return 3600 * 24 * numberOfDays;
    }

    /**
     * Calculates the end time of the batch.
     *
     * @param startTime    the start time
     * @param secondsLater number of seconds to roll forward
     * @return the end time
     */
    public static Date getSecondsLater(Date startTime, long secondsLater) {
        Instant instant = startTime.toInstant().plus(secondsLater, ChronoUnit.SECONDS);
        return Date.from(instant);
    }

    /**
     * Divides the time range into chunks. Each chunk is representing by its starting time.
     *
     * @param startTime    the start time of the time range
     * @param endTime      the end time of the time range
     * @param interval     number of interval units covered by each chunk
     * @param intervalUnit the unit used to divide the time range
     * @return a list of {@link Date} that each element represents the start time of the chunk
     */
    public static List<Pair<Date, Date>> divideTimeRange(Date startTime, Date endTime, int interval,
        ChronoUnit intervalUnit) {
        ZonedDateTime startZonedDateTime = startTime.toInstant().atZone(UTC).truncatedTo(intervalUnit);
        ZonedDateTime endZonedDateTime = endTime.toInstant().atZone(UTC).truncatedTo(intervalUnit);
        //                endTime.toInstant().atZone(DatetimeUtils.UTC).plus(interval, intervalUnit).truncatedTo(intervalUnit).minus(1, ChronoUnit.MILLIS);

        List<Pair<Date, Date>> chunks = new LinkedList<>();
        while (startZonedDateTime.isBefore(endZonedDateTime)) {
            ZonedDateTime chunkEndDateTime =
                startZonedDateTime.plus(interval, intervalUnit).minus(1, ChronoUnit.MILLIS);
            chunks.add(Pair.of(Date.from(startZonedDateTime.toInstant()), Date.from(chunkEndDateTime.toInstant())));
            startZonedDateTime = startZonedDateTime.plus(interval, intervalUnit);
        }
        return chunks;
    }

    /**
     * Divides the time range into chunks. Each chunk is representing by its starting time.
     *
     * @param startTime    the start time of the time range
     * @param endTime      the end time of the time range
     * @param interval     number of interval units covered by each chunk
     * @param intervalUnit the unit used to divide the time range
     * @return a list of {@link ZonedDateTime} that each element represents the start time of the chunk
     */
    public static List<ZonedDateTime> divideTimeRange(ZonedDateTime startTime, ZonedDateTime endTime, int interval,
        ChronoUnit intervalUnit) {
        ZonedDateTime startZonedDateTime = startTime;
        ZonedDateTime endZonedDateTime = endTime;

        List<ZonedDateTime> chunks = new LinkedList<>();
        while (!startZonedDateTime.isAfter(endZonedDateTime)) {
            chunks.add(startZonedDateTime);
            startZonedDateTime = startZonedDateTime.plus(interval, intervalUnit);
        }
        return chunks;
    }

    /**
     * Divides the date range into dates.
     *
     * @param startDate    the start date
     * @param endDate      the end date
     * @param intervalDays day interval
     * @return a list of {@link LocalDate}
     */
    public static List<LocalDate> divideDateRange(LocalDate startDate, LocalDate endDate, int intervalDays) {
        LocalDate tEndDate = startDate;
        List<LocalDate> result = new ArrayList<>();
        while (!tEndDate.isAfter(endDate)) {
            result.add(tEndDate);
            tEndDate = tEndDate.plus(intervalDays, ChronoUnit.DAYS);
        }
        return result;
    }

    /**
     * Split the date range into small chunks by the interval in milli seconds
     *
     * @param startTime            the start time of the time range
     * @param endTime              the end time of the time range
     * @param intervalMilliSeconds number of interval units covered by each chunk
     * @return a pair list of {@link Date} that each element represents the start time / end time of the chunk
     */
    public static List<Pair<Date, Date>> divideTimeRange(Date startTime, Date endTime, int intervalMilliSeconds) {

        if (ObjectUtils.isAnyNull(startTime, endTime)) {
            return Collections.emptyList();
        }
        if (intervalMilliSeconds <= 0) {
            return Collections.emptyList();
        }

        List<Pair<Date, Date>> result = new ArrayList<>();
        long start = startTime.getTime(), end = endTime.getTime();
        while (start <= end) {

            long e = start + intervalMilliSeconds;
            if (e >= end) {
                e = end;

                Pair<Date, Date> pair = Pair.of(new Date(start), new Date(e));
                result.add(pair);
                break;
            }
            //reduce 1 milliseconds for the tail of the range, except the last one
            Pair<Date, Date> pair = Pair.of(new Date(start), new Date(e - 1));
            result.add(pair);

            start += intervalMilliSeconds;
        }

        return result;
    }

    /**
     * Converts a {@link LocalDate} to a {@link Date} instance that is corresponds to the {@link LocalDate} in UTC.
     *
     * @param localDate the local date
     * @return the converted {@link Date}
     */
    public static Date toUtcDate(LocalDate localDate) {
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(UTC);
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * Returns the number of quarter corresponds to the month given.
     *
     * @param month the number of the month
     * @return the corresponding quarter
     */
    public static int getQuarterByMonth(int month) {
        switch (month) {
            case 1:
            case 2:
            case 3:
                return 1;
            case 4:
            case 5:
            case 6:
                return 2;
            case 7:
            case 8:
            case 9:
                return 3;
            case 10:
            case 11:
            case 12:
                return 4;
            default:
                throw new IllegalArgumentException("Unknown month: " + month);
        }
    }

    public static Date parseTime(String timeStr) throws ParseException {
        // this could be an epoch second or ISO8601, so we need to check it first
        if (timeStr.contains("-")) {
            SimpleDateFormat dateFormat;
            if (timeStr.contains("T")) {
                if (timeStr.contains(".")) {
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
                } else {
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
                }
            } else {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            }
            return dateFormat.parse(timeStr);
        } else {
            long epochSecond = Long.parseLong(timeStr);
            Instant instant = Instant.ofEpochSecond(epochSecond);
            return Date.from(instant);
        }
    }

    /**
     * Parse stander UTC time in valid time format with zone information.
     * <p>
     * yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     * 2011-12-03T10:15:30+01:00[Europe/Paris]
     *
     * @param dateTime the time string to parse
     * @return parsed date
     * @throws ParseException
     */
    public static Date parseFullUtcTime(String dateTime) {
        return Date.from(ZonedDateTime.parse(dateTime).toInstant());
    }

    /**
     * format the redis used date to string
     *
     * @param dateTime the date time
     * @return the redis date string
     * @throws ParseException
     */
    public static String formatFullUtcTime(Date dateTime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        return simpleDateFormat.format(dateTime);
    }

    /**
     * `     * Get a time string in one of ISO style
     *
     * @param date the date
     * @return the ISO style time string
     */
    public static String getISODateTimeString(Date date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SS");
        return simpleDateFormat.format(date);
    }

    public static List<LocalDate> getTimesBetween(LocalDate startTime, LocalDate endTime) {
        return startTime.datesUntil(endTime).collect(Collectors.toList());
    }

    /**
     * Change the time to the very beginning of a hour
     * eg, change 2018-10-23T02:32:47:00 to 2018-10-23T02:00:00:00
     *
     * @param startTime time to change
     * @return the date you want
     */
    public static Date truncateToHour(Date startTime) throws ParseException {
        return getPatternFormattedDate("yyyy-MM-dd'T'HH", startTime, "%s:00:00.000Z");
    }

    /**
     * Change the time to the very beginning of a day
     * eg, change 2018-10-23T02:32:47:00 to 2018-10-23T00:00:00:00
     *
     * @param startTime time to change
     * @return the date you want
     */
    public static Date truncateToDay(Date startTime) throws ParseException {
        return getPatternFormattedDate("yyyy-MM-dd", startTime, "%sT00:00:00.000Z");
    }

    /**
     * Change the time to the very ending of a hour
     * eg, change 2018-10-23T02:32:47:00 to 2018-10-23T02:59:59:99
     *
     * @param endTime time to change
     * @return the date you want
     */
    public static Date truncateToHourEnd(Date endTime) throws ParseException {
        return getPatternFormattedDate("yyyy-MM-dd'T'HH", endTime, "%s:59:59.999Z");
    }

    /**
     * Change the time to the very ending of a day
     * eg, change 2018-10-23T02:32:47:00 to 2018-10-23T23:59:59:99
     *
     * @param endTime time to change
     * @return the date you want
     */
    public static Date truncateToDayEnd(Date endTime) throws ParseException {
        return getPatternFormattedDate("yyyy-MM-dd", endTime, "%sT23:59:59.999Z");
    }

    /**
     * Change the time to the very beginning of a day
     * eg, change 2018-10-23T02:32:47:00 to 2018-10-23T00:00:00:000Z
     *
     * @param date time to change
     * @return the date you want
     */
    public static Date truncateToDayBegin(Date date) throws ParseException {
        return getPatternFormattedDate("yyyy-MM-dd", date, "%sT00:00:00.000Z");
    }

    private static Date getPatternFormattedDate(String keptDatePattern, Date date, String targetDatePattern)
        throws ParseException {

        SimpleDateFormat targetDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat dateFormat = new SimpleDateFormat(keptDatePattern);
        String target = String.format(targetDatePattern, dateFormat.format(date));

        return targetDateFormat.parse(target);
    }

    /**
     * merge two date into one, pick up date from first one and time from the second.
     *
     * @param date UTC date
     * @param time UTC time
     * @return merged date
     */
    public static Date mergeDateAndTime(Date date, Date time) {

        if (Objects.isNull(date)) {
            return null;
        }

        LocalDate localDate = date.toInstant().atZone(UTC).toLocalDate();
        LocalTime localTime;
        if (Objects.isNull(time)) {
            localTime = LocalTime.MIN;

        } else {
            localTime = time.toInstant().atZone(UTC).toLocalTime();
        }

        LocalDateTime localDateTime = localDate.atTime(localTime);

        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    /**
     * compare the target date with left and right to check if the date is before left, between them, or after the right
     *
     * @param left   left date
     * @param right  right date
     * @param target target date
     * @return 0:smaller, 1:between, 2:bigger
     */
    public static Integer compareDate(Date left, Date right, Date target) {

        if (ObjectUtils.isAnyNull(left, right, target)) {
            throw new IllegalArgumentException("some fields are null");
        }

        if (left.after(right)) {
            throw new IllegalArgumentException("Start date is after end date");
        }
        if (target.before(left)) {
            return 0;
        }

        if (target.after(right)) {
            return 2;
        }

        return 1;
    }

    /**
     * Get first day of current year
     *
     * @return the day
     */
    @SneakyThrows
    public static Date getFirstDayOfThisYear() {

        ZonedDateTime firstDayOfThisYear =
            ZonedDateTime.now(UTC).withMonth(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);

        return Date.from(firstDayOfThisYear.toInstant());
    }

    /**
     * Get the last milli seconds in date of this month
     *
     * @return the last milli seconds in date of this month
     */
    public static LocalDate getLastTimeEndOfSpecificMonth(LocalDate dateInMonth) {

        if (Objects.isNull(dateInMonth)) {
            return null;
        }

        return dateInMonth.with(TemporalAdjusters.lastDayOfMonth());
    }

    // TODO need to write unittest blew.

    /**
     * get the current date.
     *
     * @return
     */
    public static Date now() {

        return new Date();
    }

    /**
     * get the current UTC datetime
     *
     * @return
     */
    public static LocalDateTime nowUTC() {

        return LocalDateTime.now(ZoneId.from(ZoneOffset.UTC));
    }

    /**
     * get the current UTC second timestamp
     *
     * @return
     */
    public static Long nowUTCSecond() {

        return Instant.now().getEpochSecond();
    }

    /**
     * get the  current iso format date.
     *
     * @return
     */
    public static String nowISOString() {

        return LocalDateTime.now().format(NOW_DATE_FORMAT);
    }

    /**
     * convert date to string.
     *
     * @param date    the date
     * @param pattern the datetime format
     * @return
     */
    public static String parseDateToString(Date date, String pattern) {

        if (date == null) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
        return DateTimeFormatter.ofPattern(pattern)
            .format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * convert date to string.
     *
     * @param date    the date to parse, not null
     * @param pattern the formatter to user
     * @return
     */
    public static String parseDateToString(LocalDateTime date, String pattern) {

        if (date == null) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
        return DateTimeFormatter.ofPattern(pattern).format(date);
    }

    /**
     * convert date to string.
     *
     * @param date                the date to parse, not null
     * @param localDateFormatEnum the LocalDateFormatEnum to user
     * @return
     */
    public static String parseDateToString(Date date, LocalDateFormatEnum localDateFormatEnum) {

        if (date == null || localDateFormatEnum == null) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
        return localDateFormatEnum.getDateFormatter()
            .format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * convert string to LocalDateTime
     *
     * @param date                the text to parse, not null
     * @param localDateFormatEnum the LocalDateFormatEnum to user
     * @return
     */
    public static LocalDateTime parseLocalDateTime(String date, LocalDateFormatEnum localDateFormatEnum) {

        return LocalDateTime.parse(date, localDateFormatEnum.dateFormatter);
    }

    /**
     * convert Date to LocalDatetime
     *
     * @param date the date to parse
     * @return
     */
    public static LocalDateTime parseDateToLocalDateTime(Date date) {

        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * convert date to timestamp
     *
     * @param date the date to parse
     * @return
     */
    public static Timestamp parseTimestamp(Date date) {

        if (null == date) {
            return null;
        }
        return Timestamp.from(date.toInstant());
    }

    /**
     * convert string to timestamp
     *
     * @param date the date to parse
     * @return
     */
    public static Timestamp parseTimestamp(String date) {

        if (null == date) {
            return null;
        }
        return Timestamp.from(parseDate(date).toInstant());
    }

    /**
     * convert LocalDateTime to Timestamp
     *
     * @param date the date to parse
     * @return
     */
    public static Timestamp parseTimestamp(LocalDateTime date) {

        if (null == date) {
            return null;
        }
        return Timestamp.valueOf(date);
    }

    /**
     * support conversion format:
     * <p>
     * timestamp(1639282872000),
     * "yyyy-MM-dd'T'HH:mm:ss'Z'",
     * "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
     * "yyyy-MM-dd",
     * "yyyyMMdd"
     * </p>
     *
     * @param str the a needs to conform to Date format
     * @return
     * @throws ParseException
     */
    @SneakyThrows
    public static Date parseDate(String str) {

        int size;
        if ((size = StringUtils.length(str)) < 8) {
            return null;
        }
        if (NumberUtils.isNumber(str) && size > 8) {
            return (size == 10) ? new Date(Long.parseLong(str) * 1000L) : new Date(Long.parseLong(str));
        }
        return DateUtils.parseDate(str, CMC_IOS8601_PATTERN);
    }

    /**
     * convert LocalDateTime to Date
     *
     * @param date the date to parse
     * @return
     */
    public static Date parseDate(LocalDateTime date) {

        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * get the numbers of days between a and b
     *
     * @param a the begin date
     * @param b the end date
     * @return
     */
    public static int ofDays(Date a, Date b) {

        if (a == null || b == null) {
            return 0;
        }
        long millisTotal = b.getTime() - a.getTime();
        return getDays(millisTotal);
    }

    private static int getDays(long millisTotal) {

        if (millisTotal <= 0) {
            return 0;
        }
        int diffDays = (int)(millisTotal / (1000 * 60 * 60 * 24));
        return diffDays;
    }

    /**
     * default support datetime format
     */
    public enum LocalDateFormatEnum {
        /**
         * format：yyyy-MM-dd
         */
        YYYY_MM_DD(DateTimeFormatter.ofPattern("YYYY-MM-dd")),
        /**
         * format：yyyy-MM-dd
         */
        YYYY_MM_DD_HH_MM_SS(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        /**
         * format：yyyyMMdd
         */
        YYYYMMDD(DateTimeFormatter.ofPattern("yyyyMMdd")),
        /**
         * format：yyyyMMddHHmm
         */
        YYYYMMDDHHMM(DateTimeFormatter.ofPattern("yyyyMMddHHmm")),
        /**
         * format：yyyyMMddHHmm
         */
        YYYYMMDDHHMMSS(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),

        /**
         * format：yyyyMMddHHmm
         */
        YYYY_MM_DD_HH_MM_SS_Z(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")),
        /**
         * format：yyyy-MM-dd HH:mm
         */
        YYYY_MM_DD_HH_MM(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        final DateTimeFormatter dateFormatter;

        LocalDateFormatEnum(DateTimeFormatter dateFormatter) {
            this.dateFormatter = dateFormatter;
        }

        public DateTimeFormatter getDateFormatter() {
            return dateFormatter;
        }
    }
}
