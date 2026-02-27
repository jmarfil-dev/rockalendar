package com.jmarfildev.rockalendar.moderation.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestDataFactory;

/**
 * @author jmarfil
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
class ModerationListApiContractTest extends AbstractPostgresTest {

    @Autowired
    TestDataFactory factory;
    @Autowired
    DatabaseCleaner cleaner;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    private final String API_PENDING = "/api/moderation/events/pending";
    private final String API_ARCHIVED = "/api/moderation/events/archived";

    @BeforeEach
    void cleanDb() {
        cleaner.truncateMutableTables();
    }

    @Test
    @DisplayName("GET " + API_PENDING + " sin autenticar -> 401")
    void listPending_asAnon_401ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(API_PENDING))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, API_PENDING);
    }

    @Test
    @DisplayName("GET " + API_PENDING + " como USER -> 403")
    void listPending_asUser_403ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(API_PENDING)
                .with(contractUtils.authJwt()))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, API_PENDING);
    }

    @Test
    @DisplayName("GET " + API_PENDING + " como MODERATOR -> 200 solo eventos PENDING ordenados por submittedAt ASC")
    void listPending_asModerator_returns200OnlyPendingOrderBySubmittedAtAsc() throws Exception {
        var eventV = factory.pendingValenciaLosDeMarras();
        var eventM = factory.pendingMadridAgainstYou();
        factory.approvedMadridAgainstYou();
        factory.rejectedValenciaMafalda();

        mockMvc.perform(get(API_PENDING)
                .with(contractUtils.authJwtModerator()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(eventV.getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(eventM.getId().toString()));
    }

    @Test
    @DisplayName("GET " + API_ARCHIVED + " sin autenticar -> 401")
    void listArchived_asAnon_401ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(API_ARCHIVED))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, API_ARCHIVED);
    }

    @Test
    @DisplayName("GET " + API_ARCHIVED + " como USER -> 403")
    void listArchived_asUser_403ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(API_ARCHIVED)
                .with(contractUtils.authJwt()))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, API_ARCHIVED);
    }

    @Test
    @DisplayName("GET " + API_ARCHIVED + " como MODERATOR -> 200 solo eventos REJECTED, HIDDEN y CANCELED ordenados por moderatedAt DES")
    void listArchived_asModerator_returns200ArchivedOrderByModeratedAtDesc() throws Exception {
        factory.pendingValenciaLosDeMarras();
        factory.rejectedValenciaMafalda();
        factory.hiddenMadridSoziedadAlkoholika();
        factory.canceledBarcelonaManifa();

        mockMvc.perform(get(API_ARCHIVED).param("sort", "moderated,desc")
                .with(contractUtils.authJwtModerator()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].status").value(EventStatus.CANCELED.name()))
                .andExpect(jsonPath("$.content[1].status").value(EventStatus.HIDDEN.name()))
                .andExpect(jsonPath("$.content[2].status").value(EventStatus.REJECTED.name()));
    }
}
