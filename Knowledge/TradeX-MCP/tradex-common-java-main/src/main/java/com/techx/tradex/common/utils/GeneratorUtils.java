package com.techx.tradex.common.utils;


import java.security.SecureRandom;

public class GeneratorUtils {
    private GeneratorUtils() {
        /**/
    }

    @SuppressWarnings("squid:S3457")
    public static String genPin(int howManyDigits) {
        SecureRandom random = new SecureRandom();
        int bound = (int) Math.pow(10, howManyDigits);
        int num = random.nextInt(bound);
        return String.format("%0" + howManyDigits + "d", num);
    }
}
