package com.jmarfildev.rockalendar.moderation.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author jmarfil
 *
 */
public record ModerationApprovedListItemDto(UUID id, String title, OffsetDateTime approvedAt) {}
