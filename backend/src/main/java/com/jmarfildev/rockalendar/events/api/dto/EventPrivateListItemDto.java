package com.jmarfildev.rockalendar.events.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 *
 */
public record EventPrivateListItemDto(UUID id,
                                      String title,
                                      OffsetDateTime startDateTime,
                                      boolean startTimeUnknown,
                                      String provinceName,
                                      String cityName,
                                      boolean dateTbd,
                                      EventStatus status,
                                      String moderationMessage,
                                      OffsetDateTime submittedAt) {

    // FLAGGED es un estado interno: el usuario lo ve como PENDING_MODERATION
    public EventPrivateListItemDto {
        if (status == EventStatus.FLAGGED) {
            status = EventStatus.PENDING_MODERATION;
        }
    }
}
