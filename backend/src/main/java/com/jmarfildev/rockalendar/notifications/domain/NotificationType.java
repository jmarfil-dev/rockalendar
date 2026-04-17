package com.jmarfildev.rockalendar.notifications.domain;

/**
 * Tipos de notificación in-app que el sistema puede emitir.
 *
 * @author jmarfil
 */
public enum NotificationType {
    EVENT_PENDING_MODERATION,
    EVENT_FLAGGED,
    EVENT_APPROVED,
    EVENT_REJECTED,
    EVENT_NEEDS_CHANGES,
    EVENT_COMMENT,
    PROMOTION_REQUEST,
    USER_AUTOBANNED
}
