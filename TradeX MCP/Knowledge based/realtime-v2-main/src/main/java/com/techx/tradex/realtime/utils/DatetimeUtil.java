package com.techx.tradex.realtime.utils;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DatetimeUtil {
    public static String toDate(Date date){
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
    public String generateKey(String string, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return string + "-" + dateFormat.format(date);
    }
    public String generateKey(String string, ZonedDateTime date) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        return string + "-" + dateFormat.format(date);
    }

    public static Date toDate(String date){
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (Exception e) {
            return null;
        }
    }
}