package com.jmarfildev.rockalendar.events.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author jmarfil
 *
 */
public record EventPublicListItemDto(UUID id,
                                     String title,
                                     OffsetDateTime startDateTime,
                                     OffsetDateTime endDateTime,
                                     String provinceName,
                                     String cityName) {}
