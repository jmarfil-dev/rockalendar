package com.jmarfildev.rockalendar.moderation.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 *
 */
public record ModerationPendingListItemDto(UUID id, String title, OffsetDateTime submittedAt, UUID possibleDuplicateOf, EventStatus status) {}
