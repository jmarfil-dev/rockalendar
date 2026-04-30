package com.jmarfildev.rockalendar.notifications.application;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.notifications.domain.Notification;
import com.jmarfildev.rockalendar.notifications.persistence.NotificationRepository;

/**
 * Canal in-app: persiste la notificación en la tabla {@code notifications}.
 *
 * @author jmarfil
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationChannel implements NotificationChannel {

    private final NotificationRepository notificationRepository;

    @Override
    public void send(Notification notification) {
        notificationRepository.save(notification);
        log.debug("notificación in-app guardada type={} recipientId={} eventId={}",
                notification.getType(), notification.getRecipientId(), notification.getEventId());
    }
}
