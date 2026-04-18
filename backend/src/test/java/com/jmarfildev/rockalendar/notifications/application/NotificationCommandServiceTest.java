package com.jmarfildev.rockalendar.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.jmarfildev.rockalendar.common.error.ForbiddenException;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.notifications.domain.NotificationType;
import com.jmarfildev.rockalendar.notifications.persistence.NotificationRepository;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;

/**
 * @author jmarfil
 */
@SpringBootTest
class NotificationCommandServiceTest extends AbstractPostgresTest {

    private static final UUID USER_ID = UUID.fromString(TestConstants.MOCK_USER_ID);
    private static final UUID OTHER_USER_ID = UUID.fromString(TestConstants.MOCK_MODERATOR_ID);

    @Autowired
    NotificationCommandService commandService;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    TestDataFactory factory;
    @Autowired
    DatabaseCleaner cleaner;

    @MockitoBean
    CurrentUser currentUser;

    @BeforeEach
    void setup() {
        cleaner.truncateMutableTables();
        when(currentUser.userId()).thenReturn(USER_ID);
    }

    @Test
    @DisplayName("markAsRead: marca la notificación como leída")
    void markAsRead_setsIsReadTrue() {
        var notif = factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null,false);

        commandService.markAsRead(notif.getId());

        var updated = notificationRepository.findById(notif.getId()).orElseThrow();
        assertThat(updated.isRead()).isTrue();
    }

    @Test
    @DisplayName("markAsRead: lanza NotFoundException si la notificación no existe")
    void markAsRead_notFound_throwsNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        assertThatThrownBy(() -> commandService.markAsRead(nonExistentId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("markAsRead: lanza ForbiddenException si la notificación pertenece a otro usuario")
    void markAsRead_otherUsersNotification_throwsForbidden() {
        var notif = factory.notification(OTHER_USER_ID, NotificationType.EVENT_APPROVED, null,false);
        UUID id = notif.getId();
        assertThatThrownBy(() -> commandService.markAsRead(id))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("markAllAsRead: marca todas las no leídas del usuario como leídas")
    void markAllAsRead_marksAllUnreadForUser() {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null,false);
        factory.notification(USER_ID, NotificationType.EVENT_REJECTED, null,false);
        factory.notification(USER_ID, NotificationType.EVENT_NEEDS_CHANGES, null,true);

        commandService.markAllAsRead();

        long remaining = notificationRepository.countByRecipientIdAndIsReadFalse(USER_ID);
        assertThat(remaining).isZero();
    }

    @Test
    @DisplayName("markAllAsRead: no afecta a las notificaciones de otros usuarios")
    void markAllAsRead_doesNotAffectOtherUsers() {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null,false);
        factory.notification(OTHER_USER_ID, NotificationType.EVENT_APPROVED, null,false);

        commandService.markAllAsRead();

        long otherUnread = notificationRepository.countByRecipientIdAndIsReadFalse(OTHER_USER_ID);
        assertThat(otherUnread).isEqualTo(1);
    }
}
