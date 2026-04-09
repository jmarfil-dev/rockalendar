package com.jmarfildev.rockalendar.admin.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;
import com.jmarfildev.rockalendar.support.TestDataFactory;

/**
 * @author jmarfil
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminGetEventDetailApiContractTest extends AbstractPostgresTest {

    @Autowired
    TestDataFactory factory;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    private static final String API = "/api/admin/events/%s";

    // --- Control de acceso ---

    @Test
    @DisplayName("GET " + API + " sin autenticar -> 401")
    void getDetail_asAnon_401() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API.formatted(event.getId());

        var ra = mockMvc.perform(get(api))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("GET " + API + " como USER -> 403")
    void getDetail_asUser_403() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API.formatted(event.getId());

        var ra = mockMvc.perform(get(api)
                .with(contractUtils.authJwt()))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("GET " + API + " como MODERATOR -> 403")
    void getDetail_asModerator_403() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API.formatted(event.getId());

        var ra = mockMvc.perform(get(api)
                .with(contractUtils.authJwtModerator()))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("GET " + API + " como ADMIN, evento no existe -> 404")
    void getDetail_asAdmin_notFound_404() throws Exception {
        String api = API.formatted(UUID.randomUUID());

        var ra = mockMvc.perform(get(api)
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isNotFound());

        contractUtils.expectProblemDetail(ra, 404, api);
    }

    // --- Comportamiento ---

    @Test
    @DisplayName("GET " + API + " como ADMIN, evento APPROVED -> 200 con detalle completo")
    void getDetail_asAdmin_approved_200() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API.formatted(event.getId());

        mockMvc.perform(get(api)
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(EventStatus.APPROVED.name()))
                .andExpect(jsonPath("$.startDateTime").exists())
                .andExpect(jsonPath("$.venueName").exists())
                .andExpect(jsonPath("$.provinceId").exists())
                .andExpect(jsonPath("$.cityName").exists())
                .andExpect(jsonPath("$.artists").isArray());
    }

    @Test
    @DisplayName("GET " + API + " como ADMIN, evento PENDING -> 200")
    void getDetail_asAdmin_pending_200() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = API.formatted(event.getId());

        mockMvc.perform(get(api)
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.status").value(EventStatus.PENDING_MODERATION.name()));
    }
}
