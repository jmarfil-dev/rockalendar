package com.jmarfildev.rockalendar.common.helper;

/**
 * @author jmarfil
 *
 */
public final class StringUtils {
    private StringUtils() {}

    /**
     * Devuelve null si la cadena es null o blank.
     */
    public static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /**
     * Devuelve cadena vacía si la cadena es null o blank; en caso contrario devuelve el valor trimado.
     */
    public static String blankToEmpty(String s) {
        return (s == null || s.isBlank()) ? "" : s.trim();
    }

    /**
     * Normaliza un email: trim + toLowerCase.
     */
    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
