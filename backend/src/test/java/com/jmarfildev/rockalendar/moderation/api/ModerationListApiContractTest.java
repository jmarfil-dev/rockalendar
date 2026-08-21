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
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;
import com.jmarfildev.rockalendar.support.TestDates;

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

    private final String apiPending = "/api/moderation/events/pending";
    private final String apiArchived = "/api/moderation/events/archived";

    @BeforeEach
    void cleanDb() {
        cleaner.truncateMutableTables();
    }

    @Test
    @DisplayName("GET " + apiPending + " sin autenticar -> 401")
    void listPending_asAnon_401ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(apiPending))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, apiPending);
    }

    @Test
    @DisplayName("GET " + apiPending + " como USER -> 403")
    void listPending_asUser_403ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(apiPending)
                .with(contractUtils.authJwt()))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, apiPending);
    }

    @Test
    @DisplayName("GET " + apiPending + " como MODERATOR -> 200 solo eventos PENDING ordenados por submittedAt ASC")
    void listPending_asModerator_returns200OnlyPendingOrderBySubmittedAtAsc() throws Exception {
        var eventV = factory.pendingValenciaLosDeMarras();
        var eventM = factory.pendingMadridAgainstYou();
        factory.approvedMadridAgainstYou();
        factory.rejectedValenciaMafalda();

        mockMvc.perform(get(apiPending)
                .with(contractUtils.authJwtModerator()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(eventV.getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(eventM.getId().toString()));
    }

    @Test
    @DisplayName("GET " + apiPending + " como MODERATOR -> 200 excluye eventos pendientes con fecha pasada")
    void listPending_asModerator_excludesPastDatedEvents() throws Exception {
        var futureEvent = factory.pendingValenciaLosDeMarras();
        factory.pendingEvent("Concierto pasado", factory.valencia(), "València", "Sala Moon", TestDates.past(),
                             TestConstants.MOCK_MODERATOR_ID, TestDates.past().minusDays(1), null, null, "Grupo Pasado");

        mockMvc.perform(get(apiPending)
                .with(contractUtils.authJwtModerator()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(futureEvent.getId().toString()));
    }

    @Test
    @DisplayName("GET " + apiArchived + " sin autenticar -> 401")
    void listArchived_asAnon_401ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(apiArchived))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, apiArchived);
    }

    @Test
    @DisplayName("GET " + apiArchived + " como USER -> 403")
    void listArchived_asUser_403ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(apiArchived)
                .with(contractUtils.authJwt()))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, apiArchived);
    }

    @Test
    @DisplayName("GET " + apiArchived + " como MODERATOR -> 200 solo eventos REJECTED, HIDDEN y CANCELED ordenados por moderatedAt DESC,"
            + " excluyendo los de fecha pasada")
    void listArchived_asModerator_returns200ArchivedOrderByModeratedAtDesc() throws Exception {
        factory.pendingValenciaLosDeMarras();
        // Mafalda tiene fecha pasada (TestDates.past()): queda fuera de la bandeja de archivados,
        // que solo muestra eventos con fecha actual/futura (o dateTbd=true).
        factory.rejectedValenciaMafalda();
        factory.hiddenMadridSoziedadAlkoholika();
        factory.canceledBarcelonaManifa();

        mockMvc.perform(get(apiArchived).param("sort", "moderated,desc")
                .with(contractUtils.authJwtModerator()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].status").value(EventStatus.CANCELED.name()))
                .andExpect(jsonPath("$.content[1].status").value(EventStatus.HIDDEN.name()));
    }
}
