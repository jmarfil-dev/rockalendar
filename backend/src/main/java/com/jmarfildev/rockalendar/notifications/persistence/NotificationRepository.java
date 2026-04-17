package com.jmarfildev.rockalendar.notifications.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmarfildev.rockalendar.notifications.domain.Notification;
import com.jmarfildev.rockalendar.notifications.domain.NotificationType;

/**
 * @author jmarfil
 */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Comprueba si ya existe una notificación no leída del mismo tipo y evento
     * para el destinatario. Usado por el mecanismo de deduplicación.
     */
    boolean existsByRecipientIdAndTypeAndEventIdAndIsReadFalse(
            UUID recipientId,
            NotificationType type,
            UUID eventId);
}
