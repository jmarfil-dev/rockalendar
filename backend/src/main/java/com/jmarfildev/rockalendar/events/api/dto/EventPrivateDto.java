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
                              short provinceId,
                              String provinceName,
                              String cityName,
                              List<ArtistDto> artists,
                              String sourceUrl,
                              String posterUrl,
                              EventStatus status,
                              boolean flagged,
                              String moderationMessage,
                              OffsetDateTime createdAt,
                              OffsetDateTime submittedAt) {

    // FLAGGED es un estado interno: el usuario lo ve como PENDING_MODERATION.
    // El campo 'flagged' se calcula antes del enmascaramiento para que moderadores/admin puedan distinguirlo.
    public EventPrivateDto {
        flagged = (status == EventStatus.FLAGGED);
        if (status == EventStatus.FLAGGED) {
            status = EventStatus.PENDING_MODERATION;
        }
    }
}
