package com.jmarfildev.rockalendar.moderation.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 *
 */
public record ModerationArchivedListItemDto(UUID id,
                                            String title,
                                            EventStatus status,
                                            String moderationMessage,
                                            OffsetDateTime moderatedAt) {}
