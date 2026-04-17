package com.jmarfildev.rockalendar.notifications.application;

import com.jmarfildev.rockalendar.notifications.domain.Notification;

/**
 * Contrato para cualquier canal de envío de notificaciones.
 * Implementaciones actuales: {@link InAppNotificationChannel}.
 * Futuras: email, push, etc.
 *
 * @author jmarfil
 */
public interface NotificationChannel {

    void send(Notification notification);
}
