package com.difisoft.nhsv.admin.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateFormatUtil {

    public static String getDateString(Date date, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    public static String getDateFormat(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        return formatter.format(date);
    }

    public static String getTimeFormat(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
        return formatter.format(date);
    }

    public static String changeDateFormat(LocalDateTime dateString, String oldPattern, String newPattern) throws ParseException {
        Date date = new SimpleDateFormat(oldPattern).parse(String.valueOf(dateString));
        return new SimpleDateFormat(newPattern).format(date);
    }

    public static String getLocalDateTimeFormat(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return formatter.format(dateTime);
    }
}
