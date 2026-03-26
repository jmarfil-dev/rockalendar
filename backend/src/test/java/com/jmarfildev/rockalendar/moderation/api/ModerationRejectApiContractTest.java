package com.jmarfildev.rockalendar.moderation.api;

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
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
class ModerationRejectApiContractTest extends AbstractPostgresTest {

    @Autowired
    TestDataFactory factory;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    private final String apiReject = "/api/moderation/events/%s/reject";

    @Test
    @DisplayName("POST " + apiReject + " moderar ok -> 200 EventPrivateDto con status REJECTED")
    void reject_asModerator_ok_200ReturnsEvent() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = apiReject.formatted(event.getId());

        mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reason":"Rechazado"}
                        """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.status").value(EventStatus.REJECTED.name()))
                .andExpect(jsonPath("$.title").value(event.getTitle()));
    }

    @Test
    @DisplayName("POST " + apiReject + " reason muy largo -> 400")
    void reject_asModerator_invalidRequest_400ProblemDetail() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = apiReject.formatted(event.getId());

        String tooLong = "a".repeat(501);
        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "reason": "%s" }
                        """.formatted(tooLong)))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("POST " + apiReject + " moderar evento sin request -> 400")
    void reject_asModerator_noRequest_400ProblemDetail() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = apiReject.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("POST " + apiReject + " moderar evento sin reason -> 400")
    void reject_asModerator_noReason_400ProblemDetail() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = apiReject.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reason":""}
                        """))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("POST " + apiReject + " sin autenticar -> 401")
    void reject_asAnon_401ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000007");
        String api = apiReject.formatted(eventId);

        var ra = mockMvc.perform(post(api))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("POST " + apiReject + " como USER -> 403")
    void reject_asUser_403ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000007");
        String api = apiReject.formatted(eventId);

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("POST " + apiReject + " moderar evento no existe -> 404")
    void reject_asModerator_notFound_404ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000007");
        String api = apiReject.formatted(eventId);

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reason":"Rechzado"}
                        """))
                .andExpect(status().isNotFound());

        contractUtils.expectProblemDetail(ra, 404, api);
    }

    @Test
    @DisplayName("POST " + apiReject + " moderar status != PENDING_MODERATION -> 409")
    void reject_asModerator_wrongStatus_409ProblemDetail() throws Exception {
        var event = factory.canceledBarcelonaManifa();
        String api = apiReject.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reason":"Rechzado"}
                        """))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }

    @Test
    @DisplayName("POST " + apiReject + " moderar evento propio -> 409")
    void reject_asModerator_ownEvent_409ProblemDetail() throws Exception {
        var event = factory.pendingValenciaLosDeMarras();
        String api = apiReject.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reason":"Rechzado"}
                        """))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }
}
