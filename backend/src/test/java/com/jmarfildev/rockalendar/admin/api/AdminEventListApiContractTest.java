package com.jmarfildev.rockalendar.admin.api;

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
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminEventListApiContractTest extends AbstractPostgresTest {

    @Autowired
    TestDataFactory factory;
    @Autowired
    DatabaseCleaner cleaner;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    private static final String API = "/api/admin/events";

    @BeforeEach
    void cleanDb() {
        cleaner.truncateMutableTables();
    }

    // --- Control de acceso ---

    @Test
    @DisplayName("GET " + API + " sin autenticar -> 401")
    void list_asAnon_401ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(API))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, API);
    }

    @Test
    @DisplayName("GET " + API + " como USER -> 403")
    void list_asUser_403ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(API)
                .with(contractUtils.authJwt()))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, API);
    }

    @Test
    @DisplayName("GET " + API + " como MODERATOR -> 403")
    void list_asModerator_403ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(API)
                .with(contractUtils.authJwtModerator()))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, API);
    }

    // --- Comportamiento por defecto ---

    @Test
    @DisplayName("GET " + API + " sin filtros -> todos los estados futuros, estructura de respuesta correcta")
    void list_asAdmin_noFilters_returnsAllStatusesFuture() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        factory.approvedValenciaPast();            // pasado: no debe aparecer
        factory.pendingMadridAgainstYou();         // PENDING futuro: debe aparecer
        factory.hiddenMadridSoziedadAlkoholika();  // HIDDEN futuro: debe aparecer

        mockMvc.perform(get(API)
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].id").value(event.getId().toString()))
                .andExpect(jsonPath("$.content[0].title").exists())
                .andExpect(jsonPath("$.content[0].startDateTime").exists())
                .andExpect(jsonPath("$.content[0].provinceName").exists())
                .andExpect(jsonPath("$.content[0].status").value(EventStatus.APPROVED.name()));
    }

    @Test
    @DisplayName("GET " + API + " sin datos -> página vacía")
    void list_asAdmin_noEvents_returnsEmptyPage() throws Exception {
        mockMvc.perform(get(API)
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    // --- Filtro por estado ---

    @Test
    @DisplayName("GET " + API + "?statuses=HIDDEN -> solo HIDDEN futuros")
    void list_asAdmin_filterByHidden_returnsOnlyHiddenFuture() throws Exception {
        factory.approvedMadridAgainstYou();
        var hidden = factory.hiddenMadridSoziedadAlkoholika();

        mockMvc.perform(get(API)
                .param("statuses", EventStatus.HIDDEN.name())
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(hidden.getId().toString()))
                .andExpect(jsonPath("$.content[0].status").value(EventStatus.HIDDEN.name()));
    }

    @Test
    @DisplayName("GET " + API + "?statuses=APPROVED&statuses=HIDDEN -> ambos estados futuros")
    void list_asAdmin_filterByApprovedAndHidden_returnsBothStatuses() throws Exception {
        factory.approvedMadridAgainstYou();
        factory.hiddenMadridSoziedadAlkoholika();
        factory.pendingMadridAgainstYou(); // no solicitado

        mockMvc.perform(get(API)
                .param("statuses", EventStatus.APPROVED.name())
                .param("statuses", EventStatus.HIDDEN.name())
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    // --- Filtro por título ---

    @Test
    @DisplayName("GET " + API + "?q=against -> solo eventos con 'against' en el título (case-insensitive)")
    void list_asAdmin_filterByQuery_returnsMatchingTitle() throws Exception {
        var match = factory.approvedMadridAgainstYou(); // título contiene "Against"
        factory.approvedBarcelonaBoikot();              // no coincide

        mockMvc.perform(get(API)
                .param("q", "against")
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(match.getId().toString()));
    }

    // --- Filtro por provincia ---

    @Test
    @DisplayName("GET " + API + "?provinceId=28 -> solo eventos de Madrid")
    void list_asAdmin_filterByProvince_returnsOnlyThatProvince() throws Exception {
        var madrid = factory.approvedMadridAgainstYou();
        factory.approvedBarcelonaBoikot();

        mockMvc.perform(get(API)
                .param("provinceId", String.valueOf(TestConstants.INE_MADRID))
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(madrid.getId().toString()));
    }

    // --- Filtro por rango de fechas ---

    @Test
    @DisplayName("GET " + API + "?dateFrom&dateTo -> solo eventos dentro del rango")
    void list_asAdmin_filterByDateRange_returnsOnlyEventsInRange() throws Exception {
        factory.approvedMadridAgainstYou();   // madrid() = +1 mes, fuera del rango
        var inRange = factory.approvedBarcelonaBoikot(); // barcelona() = +2 meses, dentro del rango

        mockMvc.perform(get(API)
                .param("dateFrom", TestDates.rangeStart().toString())
                .param("dateTo", TestDates.rangeEnd().toString())
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(inRange.getId().toString()));
    }

    @Test
    @DisplayName("GET " + API + " con dateFrom > dateTo -> 400")
    void list_asAdmin_invalidDateRange_400ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(API)
                .param("dateFrom", TestDates.rangeEnd().toString())
                .param("dateTo", TestDates.rangeStart().toString())
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, API);
    }
}
