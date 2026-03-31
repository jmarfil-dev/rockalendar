package com.jmarfildev.rockalendar.events.api.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author jmarfil
 *
 */
public record EventPublicListItemDto(UUID id,
                                     String title,
                                     OffsetDateTime startDateTime,
                                     boolean startTimeUnknown,
                                     LocalDate endDate,
                                     String provinceName,
                                     String cityName,
                                     String posterUrl) {}
