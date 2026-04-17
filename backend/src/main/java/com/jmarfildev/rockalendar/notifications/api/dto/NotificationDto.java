package com.jmarfildev.rockalendar.notifications.api.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import com.jmarfildev.rockalendar.notifications.domain.NotificationType;

/**
 * @author jmarfil
 */
public record NotificationDto(
        UUID id,
        NotificationType type,
        UUID eventId,
        Map<String, String> payload,
        boolean isRead,
        OffsetDateTime createdAt) {}
