package com.jmarfildev.rockalendar.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.notifications.api.dto.NotificationDto;
import com.jmarfildev.rockalendar.notifications.domain.NotificationType;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;

/**
 * @author jmarfil
 */
@SpringBootTest
class NotificationQueryServiceTest extends AbstractPostgresTest {

    private static final UUID USER_ID = UUID.fromString(TestConstants.MOCK_USER_ID);
    private static final UUID OTHER_USER_ID = UUID.fromString(TestConstants.MOCK_MODERATOR_ID);

    @Autowired
    NotificationQueryService queryService;
    @Autowired
    TestDataFactory factory;
    @Autowired
    DatabaseCleaner cleaner;

    @BeforeEach
    void cleanDb() {
        cleaner.truncateMutableTables();
    }

    @Test
    @DisplayName("list: sin notificaciones devuelve página vacía")
    void list_empty_returnsEmptyPage() {
        var page = queryService.list(USER_ID, null, null, PageRequest.of(0, 20));
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("list: sin filtro devuelve todas las notificaciones del usuario ordenadas por fecha DESC")
    void list_noFilter_returnsAllUserNotificationsOrderedDesc() {
        OffsetDateTime older = OffsetDateTime.now().minusMinutes(5);
        OffsetDateTime newer = OffsetDateTime.now();
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false, older);
        factory.notification(USER_ID, NotificationType.EVENT_REJECTED, null, true, newer);

        var page = queryService.list(USER_ID, null, null, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(2);
        // La más reciente primero (EVENT_REJECTED)
        assertThat(page.getContent().get(0).type()).isEqualTo(NotificationType.EVENT_REJECTED);
        assertThat(page.getContent().get(1).type()).isEqualTo(NotificationType.EVENT_APPROVED);
    }

    @Test
    @DisplayName("list: con filtro por bandeja devuelve solo los tipos de esa bandeja")
    void list_withBandejaFilter_returnsOnlyBandejaTypes() {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);          // USER
        factory.notification(USER_ID, NotificationType.EVENT_REJECTED, null, false);          // USER
        factory.notification(USER_ID, NotificationType.EVENT_PENDING_MODERATION, null, false); // MODERATION

        var page = queryService.list(USER_ID, NotificationType.Bandeja.USER, null, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(2)
                                     .extracting(NotificationDto::type)
                                     .containsExactlyInAnyOrder(NotificationType.EVENT_APPROVED, NotificationType.EVENT_REJECTED);
    }

    @Test
    @DisplayName("list: bandeja tiene precedencia sobre types cuando se especifican ambos")
    void list_bandejaAndTypes_bandejaWins() {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);          // USER
        factory.notification(USER_ID, NotificationType.EVENT_PENDING_MODERATION, null, false); // MODERATION

        var page = queryService.list(USER_ID, NotificationType.Bandeja.USER,
                                     List.of(NotificationType.EVENT_PENDING_MODERATION), PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1)
                                     .extracting(NotificationDto::type)
                                     .containsOnly(NotificationType.EVENT_APPROVED);
    }

    @Test
    @DisplayName("list: con filtro por tipo (sin bandeja) devuelve solo notificaciones del tipo indicado")
    void list_withTypeFilter_returnsOnlyMatchingType() {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);
        factory.notification(USER_ID, NotificationType.EVENT_REJECTED, null, false);
        factory.notification(USER_ID, NotificationType.EVENT_NEEDS_CHANGES, null, false);

        var page = queryService.list(USER_ID, null,
                                     List.of(NotificationType.EVENT_APPROVED, NotificationType.EVENT_REJECTED),
                                     PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(2)
                                     .extracting(NotificationDto::type)
                                     .containsExactlyInAnyOrder(NotificationType.EVENT_APPROVED, NotificationType.EVENT_REJECTED);
    }

    @Test
    @DisplayName("list: no devuelve notificaciones de otros usuarios")
    void list_doesNotReturnOtherUsersNotifications() {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);
        factory.notification(OTHER_USER_ID, NotificationType.EVENT_APPROVED, null, false);

        var page = queryService.list(USER_ID, null, null, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).type()).isEqualTo(NotificationType.EVENT_APPROVED);
    }

    @Test
    @DisplayName("countUnread: devuelve 0 en todas las bandejas cuando no hay notificaciones no leídas")
    void countUnread_allRead_returnsZero() {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, true);
        factory.notification(USER_ID, NotificationType.EVENT_REJECTED, null, true);

        var result = queryService.countUnread(USER_ID);
        assertThat(result.user()).isZero();
        assertThat(result.moderation()).isZero();
        assertThat(result.admin()).isZero();
    }

    @Test
    @DisplayName("countUnread: cuenta por bandeja, ignorando leídas y otros usuarios")
    void countUnread_mixedReadAndOtherUser_returnsCorrectCountPerBandeja() {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);           // user
        factory.notification(USER_ID, NotificationType.EVENT_REJECTED, null, false);           // user
        factory.notification(USER_ID, NotificationType.EVENT_NEEDS_CHANGES, null, true);       // user, leída
        factory.notification(USER_ID, NotificationType.EVENT_PENDING_MODERATION, null, false); // moderation
        factory.notification(USER_ID, NotificationType.USER_AUTOBANNED, null, false);          // admin
        factory.notification(OTHER_USER_ID, NotificationType.EVENT_APPROVED, null, false);     // otro usuario

        var result = queryService.countUnread(USER_ID);
        assertThat(result.user()).isEqualTo(2);
        assertThat(result.moderation()).isEqualTo(1);
        assertThat(result.admin()).isEqualTo(1);
    }
}
