package com.jmarfildev.rockalendar.agenda.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.jmarfildev.rockalendar.agenda.domain.InteractionStatus;

/**
 * @author jmarfil
 */
public record AgendaItemDto(UUID eventId,
                            String title,
                            OffsetDateTime startDateTime,
                            OffsetDateTime endDateTime,
                            String venueName,
                            String cityName,
                            String provinceName,
                            InteractionStatus status,
                            OffsetDateTime createdAt) {}
