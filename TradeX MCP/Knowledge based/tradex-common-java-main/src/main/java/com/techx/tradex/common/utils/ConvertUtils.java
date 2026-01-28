package com.techx.tradex.common.utils;


import com.techx.tradex.common.exceptions.InvalidFormatException;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConvertUtils {
    public static SimpleDateFormat dateFormat() {
        return new SimpleDateFormat("yyyyMMdd");
    }

    public static SimpleDateFormat dataDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    public static SimpleDateFormat dateFormatDisplay() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    }

    public static SimpleDateFormat dateOnlyFormatDisplay() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    public static SimpleDateFormat dataDateTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public static SimpleDateFormat dateTimeFormatNumberOnly() {
        return new SimpleDateFormat("yyyyMMddHHmmss");
    }

    public static Date toEndOfDate(String date) {
        try {
            if (date == null) return null;
            return TimeUtils.getEndOfDate(dateFormat().parse(date));
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date toStartOfDate(String date) {
        try {
            if (date == null) return null;
            return TimeUtils.getStartOfDate(dateFormat().parse(date));
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date toDate(String date, String fieldName) {
        try {
            if (date == null) return null;
            return dateFormat().parse(date);
        } catch (ParseException e) {
            throw new InvalidFormatException(fieldName);
        }
    }

    public static Date toDate(String date) {
        try {
            if (date == null) return null;
            return dateOnlyFormatDisplay().parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date toDate(String date, SimpleDateFormat dateFormat) {
        try {
            if (date == null) return null;
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String fromTime(Timestamp timestamp) {
        if (timestamp == null) return null;
        return dateFormatDisplay().format(timestamp);
    }

    public static String fromDate(Date date) {
        if (date == null) return null;
        return dateOnlyFormatDisplay().format(date);
    }

    public static String fromDateTimeToNumber(Date date) {
        if (date == null) return null;
        return dateTimeFormatNumberOnly().format(date);
    }

    public static Timestamp addDay(Timestamp timestamp, long days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new java.sql.Date(timestamp.getTime()));
        calendar.add(Calendar.DATE, (int) days);
        return new Timestamp(calendar.getTimeInMillis());
    }

    public static Timestamp convertDateTime(String date, String time) {
        try {
            return new Timestamp(dataDateFormat().parse(date + " " + time).getTime());
        } catch (ParseException e) {
            throw new InvalidFormatException(date + " " + time);
        }
    }

    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "EUC-KR"), URLDecoder.decode(pair.substring(idx + 1), "EUC-KR"));
        }
        return query_pairs;
    }

    public static Timestamp fromString(String datetime) {
        try {
            return new Timestamp(dataDateTimeFormat().parse(datetime).getTime());
        } catch (ParseException e) {
            throw new InvalidFormatException(datetime);
        }
    }

    public interface IConvertType<T, S> {
        T from(S s);
    }

    public static String removeAllSpaceAndTab(String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        }
        return input.replaceAll("\\s", "");
    }

    public static String addColonToTime(String time) {
        if (StringUtils.isBlank(time)) {
            return "";
        }
        return time.replaceAll("..(?!$)", "$0:");
    }

    public static String removeColonFromTime(String time) {
        if (StringUtils.isBlank(time)) {
            return "";
        }
        return time.replace(":", "");
    }

    public static List<String> splitByComma(String input) {
        if (StringUtils.isBlank(input)) {
            new ArrayList<>();
        }
        return Arrays.asList(input.split("\\s*,\\s*"));
    }
}
