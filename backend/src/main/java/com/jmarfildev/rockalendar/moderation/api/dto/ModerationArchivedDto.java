package com.jmarfildev.rockalendar.moderation.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 *
 */
public record ModerationArchivedDto(UUID id,
                                    String title,
                                    EventStatus status,
                                    String moderationMessage,
                                    Instant moderatedAt) {}
