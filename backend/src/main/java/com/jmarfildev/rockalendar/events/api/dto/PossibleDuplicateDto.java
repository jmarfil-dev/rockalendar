package com.jmarfildev.rockalendar.events.api.dto;

import java.util.UUID;

/**
 * Información sobre un posible duplicado detectado al proponer un evento.
 * Solo se incluye {@code approved=true} (y por tanto se muestra enlace al usuario)
 * si el evento original está en estado APPROVED.
 */
public record PossibleDuplicateDto(UUID id, String title, boolean approved) {}
