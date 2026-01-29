package com.jmarfildev.rockalendar.events.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author jmarfil
 *
 */
public record EventPublicDto(UUID id,
                             String title,
                             String description,
                             OffsetDateTime startDateTime,
                             OffsetDateTime endDateTime,
                             String venueName,
                             UUID provinceId,
                             String provinceName,
                             String cityName,
                             List<String> artists,
                             String sourceUrl) {}
