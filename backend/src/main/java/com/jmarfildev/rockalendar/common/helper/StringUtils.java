package com.jmarfildev.rockalendar.common.helper;

/**
 * @author jmarfil
 *
 */
public class StringUtils {

    /**
     * Devuelve null si la cadena es null o blank.
     * @param s
     * @return
     */
    public static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
