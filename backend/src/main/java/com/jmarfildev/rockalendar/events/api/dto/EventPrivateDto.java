package com.jmarfildev.rockalendar.events.api.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 *
 */
public record EventPrivateDto(UUID id,
                              String title,
                              String description,
                              OffsetDateTime startDateTime,
                              boolean startTimeUnknown,
                              LocalDate endDate,
                              String venueName,
                              UUID provinceId,
                              String provinceName,
                              String cityName,
                              List<ArtistDto> artists,
                              String sourceUrl,
                              String posterUrl,
                              EventStatus status,
                              String moderationMessage,
                              OffsetDateTime createdAt,
                              OffsetDateTime submittedAt) {

    // FLAGGED es un estado interno: el usuario lo ve como PENDING_MODERATION
    public EventPrivateDto {
        if (status == EventStatus.FLAGGED) {
            status = EventStatus.PENDING_MODERATION;
        }
    }
}
