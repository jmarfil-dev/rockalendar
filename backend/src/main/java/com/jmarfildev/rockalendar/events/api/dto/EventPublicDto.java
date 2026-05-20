package com.jmarfildev.rockalendar.events.api.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;

/**
 * @author jmarfil
 *
 */
public record EventPublicDto(UUID id,
                             String title,
                             String description,
                             OffsetDateTime startDateTime,
                             boolean startTimeUnknown,
                             LocalDate endDate,
                             String venueName,
                             short provinceId,
                             String provinceName,
                             String cityName,
                             List<ArtistDto> artists,
                             String sourceUrl,
                             String ticketUrl,
                             String posterUrl) {}
