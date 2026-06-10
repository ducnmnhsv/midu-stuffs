package com.techx.tradex.realtime.utils;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class NumberUtils {
    public static double round2Decimal(double number) {
        return ((double) Math.round(number * 100)) / 100;
    }

    public static Double round(Integer scale, Double value) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
}

