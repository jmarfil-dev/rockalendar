package com.jmarfildev.rockalendar.events.persistence;

import java.util.UUID;

/**
 * Proyección para la detección de posibles eventos duplicados.
 */
public interface DuplicateEventProjection {

    UUID getId();

    String getTitle();

    String getStatus();
}
