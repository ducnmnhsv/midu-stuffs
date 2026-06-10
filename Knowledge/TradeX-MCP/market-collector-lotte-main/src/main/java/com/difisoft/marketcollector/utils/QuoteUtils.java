package com.difisoft.marketcollector.utils;


import org.springframework.util.StringUtils;

public class QuoteUtils {
    public static String convertAutoTime(String time) {
        if (StringUtils.isEmpty(time)) {
            return null;
        }
        StringBuilder sb = new StringBuilder(6);
        String[] temps = time.split(":");
        int hour = Integer.valueOf(temps[0]);
        hour = hour - 7;
        if (hour < 10) {
            sb.append("0");
        }
        sb.append(hour);
        sb.append(temps[1]);
        sb.append(temps[2]);
        time = sb.toString();
        return time;
    }
}
