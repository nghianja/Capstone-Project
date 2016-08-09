package com.udacity.nanodegree.nghianja.capstone.util;

/**
 * Utility class for checking/processing ISBN of books.
 */
public class Isbn {

    /**
     * Checks for ISBN 10.
     */
    public static boolean isIsbn10(String ean) {
        if (ean.length() == 10) {
            int t = 0, sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += t += Character.getNumericValue(ean.charAt(i));
            }
            if (sum % 11 == 0) return true;
        }
        return false;
    }

    /**
     * Converts ISBN 10 to ISBN 13.
     */
    public static String isbn10To13(String ean) {
        String ean13 = "978" + ean.substring(0, 9);
        int odd = 0, even = 0;
        for (int i = 0; i < ean13.length(); i++) {
            if (i % 2 == 0) odd += Character.getNumericValue(ean.charAt(i));
            else even += Character.getNumericValue(ean.charAt(i));
        }
        even *= 3;
        int check = 10 - ((odd + even) % 10);
        return ean13 + check;
    }
}
