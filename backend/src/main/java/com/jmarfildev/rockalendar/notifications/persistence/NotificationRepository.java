package com.jmarfildev.rockalendar.notifications.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    boolean existsByRecipientIdAndTypeAndEventIdAndIsReadFalse(UUID recipientId, NotificationType type, UUID eventId);

    Page<Notification> findByRecipientIdOrderByCreatedAtDescIdDesc(UUID recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndTypeInOrderByCreatedAtDescIdDesc(UUID recipientId,
                                                                            List<NotificationType> types,
                                                                            Pageable pageable);

    long countByRecipientIdAndIsReadFalse(UUID recipientId);

    long countByRecipientIdAndTypeInAndIsReadFalse(UUID recipientId, List<NotificationType> types);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId AND n.isRead = false")
    void markAllAsReadByRecipientId(@Param("recipientId") UUID recipientId);
}
