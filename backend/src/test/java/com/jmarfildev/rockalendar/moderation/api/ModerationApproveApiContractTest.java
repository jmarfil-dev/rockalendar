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
class ModerationApproveApiContractTest extends AbstractPostgresTest {

    @Autowired
    TestDataFactory factory;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    private final String apiApprove = "/api/moderation/events/%s/approve";

    @Test
    @DisplayName("POST " + apiApprove + " moderar ok -> 200 EventPrivateDto con status APPROVED")
    void approve_asModerator_ok_200ReturnsEvent() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = apiApprove.formatted(event.getId());

        mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.status").value(EventStatus.APPROVED.name()))
                .andExpect(jsonPath("$.title").value(event.getTitle()));
    }

    @Test
    @DisplayName("POST " + apiApprove + " comment muy largo -> 400")
    void approve_asModerator_invalidRequest_400ProblemDetail() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = apiApprove.formatted(event.getId());

        String tooLong = "a".repeat(501);
        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "comment": "%s" }
                        """.formatted(tooLong)))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("POST " + apiApprove + " sin autenticar -> 401")
    void approve_asAnon_401ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000007");
        String api = apiApprove.formatted(eventId);

        var ra = mockMvc.perform(post(api))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("POST " + apiApprove + " como USER -> 403")
    void approve_asUser_403ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000007");
        String api = apiApprove.formatted(eventId);

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("POST " + apiApprove + " moderar evento no existe -> 404")
    void approve_asModerator_notFound_404ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000007");
        String api = apiApprove.formatted(eventId);

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNotFound());

        contractUtils.expectProblemDetail(ra, 404, api);
    }

    @Test
    @DisplayName("POST " + apiApprove + " moderar status != PENDING_MODERATION -> 409")
    void approve_asModerator_wrongStatus_409ProblemDetail() throws Exception {
        var event = factory.canceledBarcelonaManifa();
        String api = apiApprove.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }

    @Test
    @DisplayName("POST " + apiApprove + " moderar evento propio -> 409")
    void approve_asModerator_ownEvent_409ProblemDetail() throws Exception {
        var event = factory.pendingValenciaLosDeMarras();
        String api = apiApprove.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reason":"optional"}
                        """))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }
}
