package com.jmarfildev.rockalendar.events.persistence;

import java.time.Instant;
import java.util.UUID;

/**
 * @author jmarfil
 *
 */
public interface EventPublicSearchProjection {
    UUID getId();
    String getTitle();
    Instant getStartDateTime(); // OffsetDateTime da error con nativeQuery
    Instant getEndDateTime();
    String getProvinceName();
    String getCityName();

    String getPosterUrl();
    Double getScore(); // Para ordenar por relevancia en query
}