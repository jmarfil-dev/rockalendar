package com.jmarfildev.rockalendar.users.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchiveRequest;
import com.jmarfildev.rockalendar.moderation.application.ModerationCommandService;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * Tests de integración del trust score derivado.
 * Verifica que el score se calcula correctamente sumando pesos desde action_weights.
 *
 * @author jmarfil
 */
@SpringBootTest
class TrustScoreServiceTest extends AbstractPostgresTest {

    @Autowired
    ModerationCommandService moderationService;
    @Autowired
    TrustScoreService trustScoreService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TestDataFactory factory;
    @Autowired
    DatabaseCleaner cleaner;

    @MockitoBean
    CurrentUser currentUser;

    private static final UUID USER_ID = UUID.fromString(TestConstants.MOCK_USER_ID);

    @BeforeEach
    void setup() {
        cleaner.truncateMutableTables();
        when(currentUser.userId()).thenReturn(UUID.fromString(TestConstants.MOCK_MODERATOR_ID));
    }

    @Test
    @DisplayName("sin acciones -> score es 0")
    void noActions_scoreIsZero() {
        assertThat(trustScoreService.getScore(USER_ID)).isZero();
    }

    @Test
    @DisplayName("approve -> score sube +15")
    void approve_addsApproveWeight() {
        Event event = factory.pendingMadridAgainstYou();
        moderationService.approve(event.getId(), null);
        assertThat(trustScoreService.getScore(USER_ID)).isEqualTo(15);
    }

    @Test
    @DisplayName("reject -> score baja -20")
    void reject_subtractsRejectWeight() {
        Event event = factory.pendingMadridAgainstYou();
        moderationService.reject(event.getId(), new ModerationArchiveRequest("contenido inadecuado"));
        assertThat(trustScoreService.getScore(USER_ID)).isEqualTo(-20);
    }

    @Test
    @DisplayName("requestChanges -> score baja -5")
    void requestChanges_subtractsFive() {
        Event event = factory.pendingMadridAgainstYou();
        moderationService.requestChanges(event.getId(), new ModerationArchiveRequest("falta info"));
        assertThat(trustScoreService.getScore(USER_ID)).isEqualTo(-5);
    }

    @Test
    @DisplayName("requestChanges x2 + approve -> score = -5 -5 +15 = +5")
    void requestChangesThenApprove_netScore() {
        Event event = factory.pendingMadridAgainstYou();

        moderationService.requestChanges(event.getId(), new ModerationArchiveRequest("cambio 0"));
        factory.resubmitEvent(event.getId());
        moderationService.requestChanges(event.getId(), new ModerationArchiveRequest("cambio 1"));
        factory.resubmitEvent(event.getId());
        moderationService.approve(event.getId(), null);

        // -5 -5 +15 = +5
        assertThat(trustScoreService.getScore(USER_ID)).isEqualTo(5);
    }

    @Test
    @DisplayName("requestChanges tercera vez -> auto-rechazo, score = -5 -5 -40 = -50")
    void requestChanges_thirdTime_autoRejectScore() {
        Event event = factory.pendingMadridAgainstYou();

        moderationService.requestChanges(event.getId(), new ModerationArchiveRequest("cambio 0"));
        factory.resubmitEvent(event.getId());
        moderationService.requestChanges(event.getId(), new ModerationArchiveRequest("cambio 1"));
        factory.resubmitEvent(event.getId());
        moderationService.requestChanges(event.getId(), new ModerationArchiveRequest("cambio 2"));

        // 2 × REQUEST_CHANGES (-5) + 1 × AUTO_REJECT (-40) = -50
        assertThat(trustScoreService.getScore(USER_ID)).isEqualTo(-50);
    }

    @Test
    @DisplayName("score llega a -200 (10 rechazos × -20) -> usuario queda baneado")
    void scoreReachesBanThreshold_userGetsBanned() {
        // 10 rechazos × -20 = -200 → ban
        for (int i = 0; i < 10; i++) {
            Event event = factory.pendingMadridAgainstYou();
            moderationService.reject(event.getId(), new ModerationArchiveRequest("contenido inadecuado"));
        }

        var user = userRepository.findById(USER_ID).orElseThrow();
        assertThat(user.isBanned()).isTrue();
        assertThat(trustScoreService.getScore(USER_ID)).isEqualTo(-200);
    }
}
