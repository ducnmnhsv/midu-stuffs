package com.difisoft.nhsv.admin.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class MathUtil {

    public static BigDecimal divideDoubleIgnoreNullOrZero(Double numerator, Double denominator, int scale, RoundingMode roundingMode) {
        return (Objects.isNull(numerator) || Objects.isNull(denominator) || denominator == BigDecimal.ZERO.doubleValue())
            ? BigDecimal.ZERO
            : (BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), scale, roundingMode));
    }
}
