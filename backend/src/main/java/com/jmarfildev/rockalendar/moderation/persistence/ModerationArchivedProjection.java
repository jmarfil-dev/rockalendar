package com.jmarfildev.rockalendar.moderation.persistence;

import java.time.Instant;
import java.util.UUID;

import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 *
 */
public interface ModerationArchivedProjection {
    UUID getId();
    String getTitle();
    EventStatus getStatus();
    String getModerationMessage();
    Instant getModeratedAt();
}
