package com.jmarfildev.rockalendar.admin.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 */
public record AdminEventListItemDto(
        UUID id,
        String title,
        OffsetDateTime startDateTime,
        boolean startTimeUnknown,
        String provinceName,
        boolean dateTbd,
        EventStatus status) {}
