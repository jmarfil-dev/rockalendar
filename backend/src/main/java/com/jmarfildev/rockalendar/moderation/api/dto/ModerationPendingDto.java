package com.jmarfildev.rockalendar.moderation.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * @author jmarfil
 *
 */
public record ModerationPendingDto(UUID id,
                                        String title,
                                        Instant submittedAt) {}
