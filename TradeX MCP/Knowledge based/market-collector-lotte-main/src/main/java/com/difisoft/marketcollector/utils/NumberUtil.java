package com.difisoft.marketcollector.utils;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class NumberUtil {
    public static double round2Decimal(double number) {
        BigDecimal bd = BigDecimal.valueOf(number);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double round4Decimal(double number) {
        BigDecimal bd = BigDecimal.valueOf(number);
        bd = bd.setScale(4, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double round1Decimal(double number) {
        return ((double) Math.round(number * 10)) / 10;
    }
}
