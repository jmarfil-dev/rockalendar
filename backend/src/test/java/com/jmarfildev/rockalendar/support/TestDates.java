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

    public static OffsetDateTime madrid() {
        return OffsetDateTime.now(CLOCK).plusMonths(1).withDayOfMonth(15);
    }

    public static OffsetDateTime barcelona() {
        return madrid().plusMonths(1);
    }

    public static OffsetDateTime genericFuture() {
        return OffsetDateTime.now(CLOCK).plusMonths(3).withDayOfMonth(20);
    }

    public static OffsetDateTime past() {
        return OffsetDateTime.now(CLOCK).minusMonths(1);
    }

    public static OffsetDateTime rangeStart() {
        return OffsetDateTime.now(CLOCK).plusMonths(2).withDayOfMonth(1);
    }

    public static OffsetDateTime rangeEnd() {
        return OffsetDateTime.now(CLOCK).plusMonths(2).withDayOfMonth(28);
    }
}
