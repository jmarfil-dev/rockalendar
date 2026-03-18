package com.jmarfildev.rockalendar.users.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;
import com.jmarfildev.rockalendar.support.TestDates;
import com.jmarfildev.rockalendar.users.domain.User;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * @author jmarfil
 */
@SpringBootTest
class PromotionEligibilityServiceTest extends AbstractPostgresTest {

    @Autowired
    PromotionEligibilityService service;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TestDataFactory factory;
    @Autowired
    DatabaseCleaner cleaner;

    private User eligibleUser;

    @BeforeEach
    void setup() {
        cleaner.truncateMutableTables();
        // Usuario con todos los requisitos cumplidos
        eligibleUser = factory.userWithScore(
                "eligible@test.local",
                PromotionEligibilityService.MIN_TRUST_SCORE,
                OffsetDateTime.now().minusDays(PromotionEligibilityService.MIN_SENIORITY_DAYS + 1)
        );
    }

    @Test
    @DisplayName("usuario con todos los requisitos -> elegible")
    void allRequirementsMet_isEligible() {
        assertThat(service.isEligible(eligibleUser.getId())).isTrue();
    }

    @Test
    @DisplayName("trust_score < 100 -> no elegible")
    void lowTrustScore_notEligible() {
        eligibleUser.setTrustScore(PromotionEligibilityService.MIN_TRUST_SCORE - 1);
        userRepository.save(eligibleUser);
        assertThat(service.isEligible(eligibleUser.getId())).isFalse();
    }

    @Test
    @DisplayName("antigüedad < 90 días -> no elegible")
    void tooRecent_notEligible() {
        eligibleUser.setCreatedAt(OffsetDateTime.now().minusDays(PromotionEligibilityService.MIN_SENIORITY_DAYS - 1));
        userRepository.save(eligibleUser);
        assertThat(service.isEligible(eligibleUser.getId())).isFalse();
    }

    @Test
    @DisplayName("usuario baneado -> no elegible")
    void banned_notEligible() {
        eligibleUser.setBanned(true);
        userRepository.save(eligibleUser);
        assertThat(service.isEligible(eligibleUser.getId())).isFalse();
    }

    @Test
    @DisplayName("usuario MODERATOR -> no elegible")
    void alreadyModerator_notEligible() {
        assertThat(service.isEligible(
                java.util.UUID.fromString(TestConstants.MOCK_MODERATOR_ID))).isFalse();
    }

    @Test
    @DisplayName("más de 3 eventos de la misma sala en 30 días -> no elegible")
    void tooManyRecentEventsFromSameVenue_notEligible() {
        var userId = eligibleUser.getId().toString();
        for (int i = 0; i < PromotionEligibilityService.MAX_SAME_VENUE_RECENT + 1; i++) {
            factory.approvedEvent("Evento " + i, factory.madrid(), TestConstants.MADRID,
                    "Sala Copérnico", TestDates.madrid().plusDays(i), userId, TestConstants.MOCK_ARTIST_NAME_AY);
        }
        assertThat(service.isEligible(eligibleUser.getId())).isFalse();
    }

    @Test
    @DisplayName("más de 10 eventos totales de la misma sala -> no elegible")
    void tooManyTotalEventsFromSameVenue_notEligible() {
        var userId = eligibleUser.getId().toString();
        for (int i = 0; i < PromotionEligibilityService.MAX_SAME_VENUE_TOTAL + 1; i++) {
            // Fechas pasadas para que no sean "recientes" en ventana de 30 días
            factory.approvedEvent("Evento viejo " + i, factory.madrid(), TestConstants.MADRID,
                    "Sala Copérnico", TestDates.past().minusDays(i), userId, TestConstants.MOCK_ARTIST_NAME_AY);
        }
        assertThat(service.isEligible(eligibleUser.getId())).isFalse();
    }
}
