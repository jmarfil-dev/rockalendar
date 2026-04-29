package com.jmarfildev.rockalendar.notifications.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.jmarfildev.rockalendar.users.domain.UserRole.ADMIN;
import static com.jmarfildev.rockalendar.users.domain.UserRole.MODERATOR;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.notifications.domain.Notification;
import com.jmarfildev.rockalendar.notifications.domain.NotificationType;
import com.jmarfildev.rockalendar.notifications.persistence.NotificationRepository;
import com.jmarfildev.rockalendar.users.domain.UserRole;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * Servicio central de notificaciones.
 * Itera todos los canales registrados ({@link NotificationChannel}) para cada envío,
 * lo que permite añadir canales futuros (email, push) sin modificar este servicio.
 *
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    public static final String PAYLOAD_TITLE = "title";
    public static final String PAYLOAD_REASON = "reason";
    public static final String PAYLOAD_PREVIEW = "preview";

    // Los ADMIN son también moderadores: reciben las mismas notificaciones de moderación
    private static final List<String> MODERATOR_ROLES = List.of(MODERATOR.name(), ADMIN.name());

    private final List<NotificationChannel> channels;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Envía una notificación a un usuario concreto.
     */
    @Transactional
    public void notifyUser(UUID userId, NotificationType type, UUID eventId, Map<String, String> payload) {
        dispatch(buildNotification(userId, type, eventId, payload));
    }

    /**
     * Envía una notificación en fan-out a todos los moderadores y administradores activos.
     */
    @Transactional
    public void notifyAllModerators(NotificationType type, UUID eventId, Map<String, String> payload) {
        userRepository.findByRoleInAndBannedFalseAndErasedFalse(MODERATOR_ROLES)
                      .forEach(user -> dispatch(buildNotification(user.getId(), type, eventId, payload)));
    }

    /**
     * Envía una notificación en fan-out a todos los administradores activos.
     */
    @Transactional
    public void notifyAllAdmins(NotificationType type, UUID eventId, Map<String, String> payload) {
        fanOut(UserRole.ADMIN, type, eventId, payload);
    }

    /**
     * Igual que {@link #notifyAllModerators} pero omite a los moderadores que ya
     * tengan una notificación no leída del mismo tipo y eventId (deduplicación).
     */
    @Transactional
    public void notifyModeratorsDedup(NotificationType type, UUID eventId, Map<String, String> payload) {
        userRepository.findByRoleInAndBannedFalseAndErasedFalse(MODERATOR_ROLES).forEach(moderator -> {
            boolean alreadyNotified =
                    notificationRepository.existsByRecipientIdAndTypeAndEventIdAndIsReadFalse(moderator.getId(), type, eventId);
            if (!alreadyNotified) {
                dispatch(buildNotification(moderator.getId(), type, eventId, payload));
            }
            else {
                log.debug("notificación deduplicada omitida type={} recipientId={} eventId={}", type, moderator.getId(), eventId);
            }
        });
    }

    // -------------------------------------------------------------------------

    private void fanOut(UserRole role, NotificationType type, UUID eventId, Map<String, String> payload) {
        userRepository.findByRoleAndBannedFalseAndErasedFalse(role.name())
                      .forEach(user -> dispatch(buildNotification(user.getId(), type, eventId, payload)));
    }

    private void dispatch(Notification notification) {
        channels.forEach(channel -> channel.send(notification));
    }

    private Notification buildNotification(UUID recipientId, NotificationType type, UUID eventId, Map<String, String> payload) {
        return Notification.builder()
                           .recipientId(recipientId)
                           .type(type)
                           .eventId(eventId)
                           .payload(payload != null ? payload : Map.of())
                           .build();
    }
}
