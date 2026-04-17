package com.jmarfildev.rockalendar.notifications.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.ForbiddenException;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.notifications.persistence.NotificationRepository;

/**
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationRepository repository;
    private final CurrentUser currentUser;

    @Transactional
    public void markAsRead(UUID notificationId) {
        UUID userId = currentUser.userId();
        var notification =
                repository.findById(notificationId).orElseThrow(() -> new NotFoundException(ErrorConstants.NOTIFICATION_NOT_FOUND));
        if (!notification.getRecipientId().equals(userId)) {
            throw new ForbiddenException(ErrorConstants.NOTIFICATION_NOT_OWNER);
        }
        notification.setRead(true);
    }

    @Transactional
    public void markAllAsRead() {
        repository.markAllAsReadByRecipientId(currentUser.userId());
    }
}
