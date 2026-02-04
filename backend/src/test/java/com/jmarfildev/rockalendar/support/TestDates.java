package com.jmarfildev.rockalendar.support;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Clase de utilidad para poner fechas dinámicas en los tests y que no se queden obsoletos con el paso del tiempo.
 * @author jmarfil
 *
 */
public final class TestDates {

    public static final Clock CLOCK = Clock.systemUTC();

    private TestDates() {}

    public static OffsetDateTime now() {
        return OffsetDateTime.now(CLOCK);
    }

    public static OffsetDateTime yesterday() {
        return now().minusDays(1);
    }

    public static OffsetDateTime tomorrow() {
        return now().plusDays(1);
    }

    public static OffsetDateTime madrid() {
        return now().plusMonths(1).withDayOfMonth(15);
    }

    public static OffsetDateTime barcelona() {
        return madrid().plusMonths(1);
    }

    public static OffsetDateTime genericFuture() {
        return now().plusMonths(3).withDayOfMonth(20);
    }

    public static OffsetDateTime past() {
        return now().minusMonths(1);
    }

    public static OffsetDateTime rangeStart() {
        return now().plusMonths(2).withDayOfMonth(1);
    }

    public static OffsetDateTime rangeEnd() {
        return now().plusMonths(2).withDayOfMonth(28);
    }
}
