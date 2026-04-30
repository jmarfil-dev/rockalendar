package com.jmarfildev.rockalendar.notifications.api;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.notifications.api.dto.NotificationDto;
import com.jmarfildev.rockalendar.notifications.api.dto.UnreadCountDto;
import com.jmarfildev.rockalendar.notifications.application.NotificationCommandService;
import com.jmarfildev.rockalendar.notifications.application.NotificationQueryService;
import com.jmarfildev.rockalendar.notifications.domain.NotificationType;

/**
 * @author jmarfil
 */
@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationQueryService queryService;
    private final NotificationCommandService commandService;
    private final CurrentUser currentUser;

    @Override
    public Page<NotificationDto> list(NotificationType.Bandeja bandeja, List<NotificationType> types, Pageable pageable) {
        return queryService.list(currentUser.userId(), bandeja, types, pageable);
    }

    @Override
    public UnreadCountDto unreadCount() {
        return queryService.countUnread(currentUser.userId());
    }

    @Override
    public void markAsRead(UUID id) {
        commandService.markAsRead(id);
    }

    @Override
    public void markAllAsRead() {
        commandService.markAllAsRead();
    }
}
