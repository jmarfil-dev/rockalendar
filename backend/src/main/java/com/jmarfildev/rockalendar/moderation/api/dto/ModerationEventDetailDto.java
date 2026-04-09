package com.jmarfildev.rockalendar.moderation.api.dto;

import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;

/**
 * Respuesta del endpoint de detalle de moderación.
 * Incluye el evento y, si está FLAGGED, el motivo de la marca automática.
 *
 * @author jmarfil
 */
public record ModerationEventDetailDto(EventPrivateDto event, FlagInfoDto flagInfo) {}
