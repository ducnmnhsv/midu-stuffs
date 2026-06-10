package com.techx.tradex.common.utils;


public class DoubleUtils {
    public static Double plus(Double a, Double b) {
        if (a == null) return b;
        if (b == null) return a;
        return a + b;
    }

    public static double plusOrZero(Double a, Double b) {
        Double c = plus(a, b);
        return c == null ? 0d : c;
    }

    public static Double min(Double a, Double b) {
        return a == null ? b : (b == null ? a : (a < b ? a : b));
    }

    public static double minOrZero(Double a, Double b) {
        Double c = min(a, b);
        return c == null ? 0d : c;
    }

    public static double maxOrZero(Double a, Double b) {
        Double c = max(a, b);
        return c == null ? 0d : c;
    }

    public static Double max(Double a, Double b) {
        return a == null ? b : (b == null ? a : (a < b ? b : a));
    }

    public static double defaultZero(Double a) {
        return a == null ? 0d : a.doubleValue();
    }
}
