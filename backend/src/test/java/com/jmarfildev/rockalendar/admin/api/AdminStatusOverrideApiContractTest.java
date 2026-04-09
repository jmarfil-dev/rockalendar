package com.jmarfildev.rockalendar.admin.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class AdminStatusOverrideApiContractTest extends AbstractPostgresTest {

    @Autowired
    TestDataFactory factory;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    private static final String API = "/api/admin/events/%s/status";

    @Test
    @DisplayName("POST " + API + " como ADMIN, transición válida -> 200 con nuevo status")
    void override_asAdmin_validTransition_200ReturnsEvent() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = API.formatted(event.getId());

        mockMvc.perform(post(api)
                .with(contractUtils.authJwtAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"targetStatus":"APPROVED","reason":"Aprobado directamente por admin"}
                        """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.status").value(EventStatus.APPROVED.name()));
    }

    @Test
    @DisplayName("POST " + API + " como ADMIN, sin reason -> 200 (reason opcional)")
    void override_asAdmin_noReason_200OK() throws Exception {
        var event = factory.rejectedValenciaMafalda();
        String api = API.formatted(event.getId());

        mockMvc.perform(post(api)
                .with(contractUtils.authJwtAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"targetStatus":"PENDING_MODERATION"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EventStatus.PENDING_MODERATION.name()));
    }

    @Test
    @DisplayName("POST " + API + " sin autenticar -> 401")
    void override_asAnon_401ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000001");
        String api = API.formatted(eventId);

        var ra = mockMvc.perform(post(api)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"targetStatus":"APPROVED"}
                        """))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("POST " + API + " como USER -> 403")
    void override_asUser_403ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000001");
        String api = API.formatted(eventId);

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"targetStatus":"APPROVED"}
                        """))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("POST " + API + " como MODERATOR -> 403")
    void override_asModerator_403ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000001");
        String api = API.formatted(eventId);

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"targetStatus":"APPROVED"}
                        """))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("POST " + API + " evento no existe -> 404")
    void override_asAdmin_notFound_404ProblemDetail() throws Exception {
        UUID eventId = UUID.randomUUID();
        String api = API.formatted(eventId);

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"targetStatus":"APPROVED"}
                        """))
                .andExpect(status().isNotFound());

        contractUtils.expectProblemDetail(ra, 404, api);
    }

    @Test
    @DisplayName("POST " + API + " targetStatus requerido -> 400")
    void override_asAdmin_missingTargetStatus_400ProblemDetail() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reason":"falta el estado destino"}
                        """))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("POST " + API + " desde ERASED (terminal) -> 409")
    void override_asAdmin_fromErased_409ProblemDetail() throws Exception {
        var event = factory.erasedSevillaLaPolla();
        String api = API.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"targetStatus":"APPROVED","reason":"intentando recuperar evento borrado"}
                        """))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }

    @Test
    @DisplayName("POST " + API + " hacia ERASED (terminal) -> 409")
    void override_asAdmin_toErased_409ProblemDetail() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"targetStatus":"ERASED","reason":"intentando borrar via admin"}
                        """))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }
}
