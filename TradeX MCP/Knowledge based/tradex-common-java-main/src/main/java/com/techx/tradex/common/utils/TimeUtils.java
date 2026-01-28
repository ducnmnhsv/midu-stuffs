package com.techx.tradex.common.utils;


import org.apache.commons.lang3.time.DateUtils;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static final long DAY_IN_MS = 86400000;
    public static final String DATE_DISPLAY_FORMAT = "YYYYMMDD";
    public static final String DATETIME_DISPLAY_FORMAT = "YYYYMMDDhhmmss";
    public static final String TIME_DISPLAY_FORMAT = "hhmmss";

    public static Date todayBegin() {
        return startOfPrevious(0);
    }

    public static Date todayEnd() {
        return endOfPrevious(0);
    }

    public static Date yesterdayBegin() {
        return startOfPrevious(1);
    }

    public static Date yesterdayEnd() {
        return endOfPrevious(1);
    }

    public static Date previous(int range) {
        return new Date(System.currentTimeMillis() - ((long) range) * 86400000l);
    }

    public static Date endOfPrevious(int range) {
        return getEndOfDate(previous(range));
    }

    public static Date startOfPrevious(int range) {
        return getStartOfDate(previous(range));
    }

    public static Date getEndOfDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    public static Date getEndOfDateWithoutMillis(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date getStartOfDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date getStartOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date removeMillisecond(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getEndOfWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }


    public static Date getStartOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getEndOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    public static boolean sameDay(Timestamp t1, Timestamp t2) {
        return getStartOfDate(new Date(t1.getTime())).getTime() == getStartOfDate(new Date(t2.getTime())).getTime();
    }

    public static int compareDateOnly(Date date1, Date date2) {
        return DateUtils.truncatedCompareTo(date1, date2, Calendar.DATE);
    }

    public static int compareWeekOnly(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.WEEK_OF_YEAR) - cal2.get(Calendar.WEEK_OF_YEAR);
    }

    public static int compareMonthOnly(Date date1, Date date2) {
        return DateUtils.truncatedCompareTo(date1, date2, Calendar.MONTH);
    }

    public static Date getCurrentDateFromTime(String time) {
        Calendar calendar = Calendar.getInstance();
        LocalTime localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss"));
        calendar.set(Calendar.HOUR_OF_DAY, localTime.getHour());
        calendar.set(Calendar.MINUTE, localTime.getMinute());
        calendar.set(Calendar.SECOND, localTime.getSecond());
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static boolean isSameHourAndMinute(String time1, String time2) {
        String hourMinute1 = time1.substring(0, 4);
        String hourMinute2 = time2.substring(0, 4);
        return hourMinute1.equals(hourMinute2);
    }

    public static boolean isWeekend(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    public static Date timestampToDate(long time) {
        Timestamp timestamp = new Timestamp(time);
        return new Date(timestamp.getTime());
    }
}
