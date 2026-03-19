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
 * Tests de integración del trust score. Verifica los deltas aplicados al creador del evento
 * tras cada acción de moderación.
 *
 * @author jmarfil
 */
@SpringBootTest
class TrustScoreServiceTest extends AbstractPostgresTest {

    @Autowired
    ModerationCommandService moderationService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TestDataFactory factory;
    @Autowired
    DatabaseCleaner cleaner;

    @MockitoBean
    CurrentUser currentUser;

    @BeforeEach
    void setup() {
        cleaner.truncateMutableTables();
        when(currentUser.userId()).thenReturn(UUID.fromString(TestConstants.MOCK_MODERATOR_ID));
    }

    @Test
    @DisplayName("approve sin cambios previos -> +10 al creador")
    void approve_noPriorChanges_addsTen() {
        Event event = factory.pendingMadridAgainstYou();
        int scoreBefore = scoreOf(TestConstants.MOCK_USER_ID);

        moderationService.approve(event.getId(), null);

        assertThat(scoreOf(TestConstants.MOCK_USER_ID)).isEqualTo(scoreBefore + TrustScoreService.DELTA_APPROVED_DIRECT);
    }

    @Test
    @DisplayName("approve tras solicitar cambios -> +5 al creador")
    void approve_afterRequestChanges_addsFive() {
        Event event = factory.pendingMadridAgainstYou();
        int scoreBefore = scoreOf(TestConstants.MOCK_USER_ID);

        // Primero solicitar cambios, luego re-poner el evento en PENDING y aprobar
        moderationService.requestChanges(event.getId(), new ModerationArchiveRequest("falta info"));
        factory.resubmitEvent(event.getId());

        moderationService.approve(event.getId(), null);

        assertThat(scoreOf(TestConstants.MOCK_USER_ID)).isEqualTo(scoreBefore + TrustScoreService.DELTA_APPROVED_AFTER_CHANGES);
    }

    @Test
    @DisplayName("reject sin cambios previos -> -15 al creador")
    void reject_noPriorChanges_subtractsFifteen() {
        Event event = factory.pendingMadridAgainstYou();
        int scoreBefore = scoreOf(TestConstants.MOCK_USER_ID);

        moderationService.reject(event.getId(), new ModerationArchiveRequest("contenido inadecuado"));

        assertThat(scoreOf(TestConstants.MOCK_USER_ID)).isEqualTo(scoreBefore + TrustScoreService.DELTA_REJECTED);
    }

    @Test
    @DisplayName("requestChanges tercera vez -> auto-rechazo y -30 al creador")
    void requestChanges_thirdTime_autoRejectsSubtractsThirty() {
        Event event = factory.pendingMadridAgainstYou();
        int scoreBefore = scoreOf(TestConstants.MOCK_USER_ID);

        // Primera y segunda rondas: REQUEST_CHANGES normal, sin penalización
        moderationService.requestChanges(event.getId(), new ModerationArchiveRequest("cambio 0"));
        factory.resubmitEvent(event.getId());
        moderationService.requestChanges(event.getId(), new ModerationArchiveRequest("cambio 1"));
        factory.resubmitEvent(event.getId());

        // Tercera vez: rechazo automático con penalización máxima
        moderationService.requestChanges(event.getId(), new ModerationArchiveRequest("cambio 2"));

        assertThat(scoreOf(TestConstants.MOCK_USER_ID)).isEqualTo(scoreBefore + TrustScoreService.DELTA_REJECTED_AFTER_MANY_CHANGES);
    }

    @Test
    @DisplayName("requestChanges no modifica el trust score")
    void requestChanges_doesNotChangeScore() {
        Event event = factory.pendingMadridAgainstYou();
        int scoreBefore = scoreOf(TestConstants.MOCK_USER_ID);

        moderationService.requestChanges(event.getId(), new ModerationArchiveRequest("falta info"));

        assertThat(scoreOf(TestConstants.MOCK_USER_ID)).isEqualTo(scoreBefore);
    }

    @Test
    @DisplayName("score llega a -200 -> usuario queda con banned=true")
    void scoreReachesBanThreshold_userGetsBanned() {
        // Ajusta el score del usuario seed a -185 (un rechazo directo de -15 llega a -200)
        userRepository.findById(UUID.fromString(TestConstants.MOCK_USER_ID)).ifPresent(u -> {
            u.setTrustScore(-185);
            userRepository.save(u);
        });

        Event event = factory.pendingMadridAgainstYou();
        moderationService.reject(event.getId(), new ModerationArchiveRequest("contenido inadecuado"));

        var user = userRepository.findById(UUID.fromString(TestConstants.MOCK_USER_ID)).orElseThrow();
        assertThat(user.isBanned()).isTrue();
    }

    private int scoreOf(String userId) {
        return userRepository.findById(UUID.fromString(userId)).orElseThrow().getTrustScore();
    }
}
