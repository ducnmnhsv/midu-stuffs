package com.techx.tradex.common.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NumberUtils {
    public static Double round2Decimal(Double number) {
        return round2Decimal(number, null);
    }

    public static Double round2Decimal(Double number, Double defaultValue) {
        if (number == null) {
            return defaultValue;
        }
        return ((double) Math.round(number * 100)) / 100;
    }

    public static Double round2DecimalFloatToDouble(Float number) {
        if (number == null) {
            return null;
        }
        return round2Decimal((double) number);

    }

    public static Double round1Decimal(Double number) {
        if (number == null) {
            return null;
        }
        return ((double) Math.round(number * 10)) / 10;
    }

    public static Double round1DecimalFloatToDouble(Float number) {
        if (number == null) {
            return null;
        }
        return round1Decimal((double) number);
    }

    public static boolean nullOrEqual(Double number, double compare) {
        return number == null || number == compare;
    }

    public static boolean nullOrEqual(Long number, long compare) {
        return number == null || number == compare;
    }

}

