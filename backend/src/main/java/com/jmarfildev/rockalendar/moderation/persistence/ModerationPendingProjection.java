package com.jmarfildev.rockalendar.moderation.persistence;

import java.time.Instant;
import java.util.UUID;

/**
 * @author jmarfil
 *
 */
public interface ModerationPendingProjection {
    UUID getId();
    String getTitle();
    Instant getSubmittedAt();
}
