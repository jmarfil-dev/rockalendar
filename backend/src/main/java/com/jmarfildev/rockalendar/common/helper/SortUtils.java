package com.jmarfildev.rockalendar.common.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utilidades para traducir ordenaciones del front a:
 * - Sort JPA (JPQL/Criteria): usando property paths (ej. "province.name")
 * - SortChoice SQL (native con ORDER BY CASE): usando sortKey/sortDir whitelist
 *
 * Objetivos:
 * - Whitelist: ignorar campos no permitidos (o caer en defaults)
 * - Defaults consistentes
 * - Estabilizar paginación añadiendo un tie-breaker (id) en JPA
 *
 * @author jmarfil
 *
 */
public final class SortUtils {

    private SortUtils() {}

    /**
     * Traduce pageable.sort a un Sort JPA aplicando un mapa de campos permitidos -> property path real.
     * Si no hay ninguno válido, devuelve defaultSort.
     * Siempre añade tieBreaker (por ejemplo "id") al final si no viene ya.
     *
     * @param pageable      Pageable del controller
     * @param allowedMap    key externa -> property path JPA (ej "province"->"province.name")
     * @param defaultSort   sort por defecto si no hay sort válido (no null)
     * @param tieBreaker    propiedad JPA para estabilizar (ej "id"), puede ser null para no añadir
     */
    public static Sort toJpaSort(Pageable pageable, Map<String, String> allowedMap, Sort defaultSort, String tieBreaker) {
        Sort mapped = mapAllowedJpaSort(pageable != null ? pageable.getSort() : Sort.unsorted(), allowedMap);
        Sort sort = (mapped == null) ? defaultSort : mapped;

        if (tieBreaker != null && sort.getOrderFor(tieBreaker) == null) {
            sort = sort.and(Sort.by(Sort.Order.asc(tieBreaker)));
        }

        return sort;
    }

    private static Sort mapAllowedJpaSort(Sort inSort, Map<String, String> allowedMap) {
        if (inSort.isUnsorted()) {
            return null;
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order o : inSort) {
            String mapped = allowedMap.get(o.getProperty());
            if (mapped == null) {
                // Si el orden no existe en allowedMap, lo ignora
                continue;
            }
            orders.add(new Sort.Order(o.getDirection(), mapped));
        }

        return orders.isEmpty() ? null : Sort.by(orders);
    }

    /**
     * Traduce pageable.sort a un SortChoice para SQL (native), usando:
     * - allowedKeys: conjunto de sortKey permitidas en SQL (ej: title,date,province,city,relevance)
     * - aliases: permite traducir propiedades alternativas del front a tu sortKey canónica (ej: startDateTime -> date)
     * Si no hay sort válido, devuelve defaultKey/defaultDir.
     *
     * IMPORTANTE:
     * - Por defecto tomamos SOLO el primer Sort.Order (lo normal para SQL CASE).
     *
     * @param pageable      Pageable del controller
     * @param allowedKeys   keys permitidas para SQL
     * @param aliases       alias -> key canónica (ej "startDateTime"->"date")
     * @param defaultKey    default sortKey si no hay válido (no null)
     * @param defaultDir    default direction ("asc" o "desc") si no hay válido (no null)
     */
    public static SortChoice toSqlSortChoice(Pageable pageable,
                                             Set<String> allowedKeys,
                                             Map<String, String> aliases,
                                             String defaultKey,
                                             String defaultDir) {
        String key = null;
        String dir = null;

        if (pageable != null && pageable.getSort() != null && pageable.getSort().isSorted()) {
            Sort.Order first = pageable.getSort().iterator().next();
            key = normalizeSqlKey(first.getProperty(), aliases);
            dir = first.getDirection().isAscending() ? "asc" : "desc";

            if (key == null || !allowedKeys.contains(key)) {
                key = null;
                dir = null;
            }
        }

        if (key == null) {
            key = defaultKey;
            dir = defaultDir;
        }

        // fuerza formato esperado
        key = key.toLowerCase();
        dir = dir.toLowerCase();
        if (!allowedKeys.contains(key)) {
            key = defaultKey.toLowerCase();
        }
        if (!dir.equals("asc") && !dir.equals("desc")) {
            dir = defaultDir.toLowerCase();
        }

        return new SortChoice(key, dir);
    }

    private static String normalizeSqlKey(String raw, Map<String, String> aliases) {
        if (raw == null) {
            return null;
        }
        if (aliases.containsKey(raw)) {
            return aliases.get(raw);
        }
        return raw;
    }

    public record SortChoice(String sortKey, String sortDir) {}
}
