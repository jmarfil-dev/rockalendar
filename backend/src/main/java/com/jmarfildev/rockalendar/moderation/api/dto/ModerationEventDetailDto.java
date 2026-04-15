package com.jmarfildev.rockalendar.moderation.api.dto;

import java.util.UUID;

import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;

/**
 * Respuesta del endpoint de detalle de moderación.
 * Incluye el evento y, si está FLAGGED, el motivo de la marca automática.
 * {@code possibleDuplicateOf} es el ID del evento que se detectó como posible duplicado, o null.
 *
 * @author jmarfil
 */
public record ModerationEventDetailDto(EventPrivateDto event, FlagInfoDto flagInfo, UUID possibleDuplicateOf) {}
