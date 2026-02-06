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
class ModerationHideApiContractTest extends AbstractPostgresTest {

    @Autowired
    TestDataFactory factory;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    private final String API_HIDE = "/api/moderation/events/%s/hide";

    @Test
    @DisplayName("POST " + API_HIDE + " moderar ok -> 200 EventPrivateDto con status HIDDEN")
    void hide_asModerator_ok_200ReturnsEvent() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = API_HIDE.formatted(event.getId());

        mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reason":"Rechazado"}
                        """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.status").value(EventStatus.HIDDEN.name()))
                .andExpect(jsonPath("$.title").value(event.getTitle()));
    }

    @Test
    @DisplayName("POST " + API_HIDE + " reason muy largo -> 400")
    void hide_asModerator_invalidRequest_400ProblemDetail() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = API_HIDE.formatted(event.getId());

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
    @DisplayName("POST " + API_HIDE + " moderar evento sin request -> 400")
    void hide_asModerator_noRequest_400ProblemDetail() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = API_HIDE.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("POST " + API_HIDE + " moderar evento sin reason -> 400")
    void hide_asModerator_noReason_400ProblemDetail() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = API_HIDE.formatted(event.getId());

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
    @DisplayName("POST " + API_HIDE + " sin autenticar -> 401")
    void hide_asAnon_401ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000007");
        String api = API_HIDE.formatted(eventId);

        var ra = mockMvc.perform(post(api))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("POST " + API_HIDE + " como USER -> 403")
    void hide_asUser_403ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000007");
        String api = API_HIDE.formatted(eventId);

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("POST " + API_HIDE + " moderar evento no existe -> 404")
    void hide_asModerator_notFound_404ProblemDetail() throws Exception {
        UUID eventId = UUID.fromString("cccccccc-0000-0000-0000-000000000007");
        String api = API_HIDE.formatted(eventId);

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reason":"Ocultado"}
                        """))
                .andExpect(status().isNotFound());

        contractUtils.expectProblemDetail(ra, 404, api);
    }

    @Test
    @DisplayName("POST " + API_HIDE + " moderar status != PENDING_MODERATION -> 409")
    void hide_asModerator_wrongStatus_409ProblemDetail() throws Exception {
        var event = factory.canceledBarcelonaManifa();
        String api = API_HIDE.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reason":"Ocultado"}
                        """))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }

    @Test
    @DisplayName("POST " + API_HIDE + " moderar evento propio -> 409")
    void hide_asModerator_ownEvent_409ProblemDetail() throws Exception {
        var event = factory.pendingValenciaLosDeMarras();
        String api = API_HIDE.formatted(event.getId());

        var ra = mockMvc.perform(post(api)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reason":"Ocultado"}
                        """))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }
}
