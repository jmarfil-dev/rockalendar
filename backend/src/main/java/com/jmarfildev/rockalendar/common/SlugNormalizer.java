package com.jmarfildev.rockalendar.common;

import java.text.Normalizer;

/**
 * @author jmarfil
 *
 */
public final class SlugNormalizer {
    private SlugNormalizer() {}

    public static String of(String input) {
        if (input == null) {
            return "";
        }
        String s = input.trim().toLowerCase();

        // Quita tildes y otros signos diacríticos
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        // Elimina símbolos
        s = s.replaceAll("[^a-z0-9]+", "");
        // Elimina espacios
        s = s.replaceAll("\\s+", "").trim();

        return s;
    }
}
