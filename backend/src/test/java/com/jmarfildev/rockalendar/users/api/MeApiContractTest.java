package com.jmarfildev.rockalendar.users.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;
import com.jmarfildev.rockalendar.users.application.PromotionEligibilityService;
import com.jmarfildev.rockalendar.users.domain.UserRole;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * @author jmarfil
 */
@SpringBootTest
@AutoConfigureMockMvc
class MeApiContractTest extends AbstractPostgresTest {

    private final String apiMe = "/api/me";
    private final String apiPromotion = "/api/me/promotion-request";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    TestDataFactory factory;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DatabaseCleaner cleaner;

    @BeforeEach
    void setup() {
        cleaner.truncateMutableTables();
    }

    // --- GET /api/me ---

    @Test
    @DisplayName("GET /api/me autenticado -> 200 con datos del usuario")
    void getMe_authenticated_200WithUserData() throws Exception {
        mockMvc.perform(get(apiMe).with(contractUtils.authJwt()))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.email").value(TestConstants.MOCK_USER_EMAIL))
               .andExpect(jsonPath("$.role").value(UserRole.USER.name()))
               .andExpect(jsonPath("$.promotionEligible").isBoolean());
    }

    @Test
    @DisplayName("GET /api/me sin autenticar -> 401")
    void getMe_anonymous_401() throws Exception {
        var ra = mockMvc.perform(get(apiMe))
                        .andExpect(status().isUnauthorized());
        contractUtils.expectProblemDetail(ra, 401, apiMe);
    }

    // --- POST /api/me/promotion-request ---

    @Test
    @DisplayName("POST /api/me/promotion-request usuario elegible -> 200 con role MODERATOR")
    void requestPromotion_eligible_200AndRoleChanged() throws Exception {
        var eligible = factory.userWithScore(
                "promo@test.local",
                PromotionEligibilityService.MIN_TRUST_SCORE,
                OffsetDateTime.now().minusDays(PromotionEligibilityService.MIN_SENIORITY_DAYS + 1)
        );

        mockMvc.perform(post(apiPromotion)
                       .with(contractUtils.authJwt(eligible.getId().toString(),
                               eligible.getEmail(), "ROLE_USER")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.role").value(UserRole.MODERATOR.name()))
               .andExpect(jsonPath("$.promotionEligible").value(false));
    }

    @Test
    @DisplayName("POST /api/me/promotion-request usuario no elegible -> 409")
    void requestPromotion_notEligible_409() throws Exception {
        // El usuario seed MOCK_USER tiene trust_score=10, insuficiente
        var ra = mockMvc.perform(post(apiPromotion).with(contractUtils.authJwt()))
                        .andExpect(status().isConflict());
        contractUtils.expectProblemDetail(ra, 409, apiPromotion);
    }

    @Test
    @DisplayName("POST /api/me/promotion-request sin autenticar -> 401")
    void requestPromotion_anonymous_401() throws Exception {
        var ra = mockMvc.perform(post(apiPromotion))
                        .andExpect(status().isUnauthorized());
        contractUtils.expectProblemDetail(ra, 401, apiPromotion);
    }
}
