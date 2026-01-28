package com.techx.tradex.common.utils;

public class StringUtils {
    public static boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }

    public static boolean isNotEmpty(String input) {
        return !isEmpty(input);
    }

    public static String getDisplayName(String firstName, String lastName) {
        if (isEmpty(firstName)) {
            return isEmpty(lastName) ? null : lastName;
        } else {
            return isEmpty(lastName) ? firstName : String.format("%s %s", lastName, firstName);
        }
    }

    public static String replaceAll(String object, Character search, String replace) {
        int objectLength = object.length();
        StringBuilder sb = new StringBuilder(objectLength * replace.length());
        for (int i = 0; i < objectLength; i++) {
            Character c = object.charAt(i);
            if (c == search) {
                sb.append(replace);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
