package com.jmarfildev.rockalendar.events.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author jmarfil
 */
public record EventCommentDto(
    UUID id,
    String authorEmail,
    String authorName,
    String body,
    OffsetDateTime createdAt
) {}
