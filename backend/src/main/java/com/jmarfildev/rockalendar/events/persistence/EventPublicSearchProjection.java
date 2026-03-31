package com.jmarfildev.rockalendar.events.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * @author jmarfil
 *
 */
public interface EventPublicSearchProjection {
    UUID getId();
    String getTitle();
    Instant getStartDateTime(); // OffsetDateTime da error con nativeQuery
    LocalDate getEndDate();
    String getProvinceName();
    String getCityName();

    String getPosterUrl();
    boolean isStartTimeUnknown();
    Double getScore(); // Para ordenar por relevancia en query
}