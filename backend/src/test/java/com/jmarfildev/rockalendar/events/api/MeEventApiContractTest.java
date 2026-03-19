package com.jmarfildev.rockalendar.events.api;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;
import com.jmarfildev.rockalendar.support.TestDates;

/**
 * @author jmarfil
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
class MeEventApiContractTest extends AbstractPostgresTest {

    @Autowired
    TestDataFactory factory;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    private final String API_ME_EVENTS = "/api/me/events";

    @Test
    @DisplayName("GET /api/me/events sin auth -> 401 ProblemDetail")
    void getMeEvents_asAnon_returns401ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(API_ME_EVENTS));
        contractUtils.expectProblemDetail(ra, 401, API_ME_EVENTS);
    }

    @Test
    @DisplayName("GET /api/me/events con auth -> 200 (lista vacía OK)")
    void getMeEvents_asUser_returns200() throws Exception {
        mockMvc.perform(get(API_ME_EVENTS).with(contractUtils.authJwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/me/events con auth -> 201 EventPrivateDto con status PENDING_MODERATION")
    void propose_asUser_returns201WithPendingEvent() throws Exception {
        var body = eventBody(factory.sevilla().getId().toString(), "[\"%s\"]".formatted(TestConstants.MOCK_ARTIST_NAME_AY));

        mockMvc.perform(post(API_ME_EVENTS)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.event.id").isNotEmpty())
                .andExpect(jsonPath("$.event.status").value(EventStatus.PENDING_MODERATION.name()))
                .andExpect(jsonPath("$.event.title").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/me/events sin auth -> 401 ProblemDetail")
    void propose_asAnon_returns401ProblemDetail() throws Exception {
        String artists = "[%s]".formatted(TestConstants.MOCK_ARTIST_NAME_AY);
        var body = eventBody(factory.sevilla().getId().toString(), artists);

        var ra = mockMvc.perform(post(API_ME_EVENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        contractUtils.expectProblemDetail(ra, 401, API_ME_EVENTS);
    }

    @Test
    @DisplayName("POST /api/me/events con auth pero artists vacío -> 400 ProblemDetail")
    void propose_asUser_emptyArtists_returns400ProblemDetail() throws Exception {
        var body = eventBody(factory.sevilla().getId().toString(), "[]");

        var ra = mockMvc.perform(post(API_ME_EVENTS)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        contractUtils.expectProblemDetail(ra, 400, API_ME_EVENTS);
    }

    @Test
    @DisplayName("PUT /api/me/events/{eventId} con auth -> 200 EventPrivateDto con status PENDING_MODERATION")
    void update_asOwner_returns200WithPendingEvent() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API_ME_EVENTS.concat("/" + event.getId());
        var body = eventBody(factory.sevilla().getId().toString(), "[\"%s\"]".formatted(TestConstants.MOCK_ARTIST_NAME_AY));

        mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value(EventStatus.PENDING_MODERATION.name()))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.title").value(not(event.getTitle())));
    }

    @Test
    @DisplayName("PUT /api/me/events/{eventId} sin body -> 400 ProblemDetail")
    void update_asOwner_noBody_returns400ProblemDetail() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API_ME_EVENTS.concat("/" + event.getId());

        var ra = mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("PUT /api/me/events/{eventId} endDateTime < startDateTime -> 400 ProblemDetail")
    void update_asOwner_invalidDateRange_returns400ProblemDetail() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API_ME_EVENTS.concat("/" + event.getId());

        String body = """
                {
                  "title": "Nuevo título",
                  "description": "desc",
                  "startDateTime": "2026-03-10T20:00:00Z",
                  "endDateTime": "2026-03-10T19:00:00Z",
                  "venueName": "Sala X",
                  "provinceId": "%s",
                  "cityName": "Madrid",
                  "artists": ["Band A"],
                  "sourceUrl": "https://example.com"
                }
                """.formatted(factory.madrid().getId());

        var ra = mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("PUT /api/me/events/{eventId} sin auth -> 401 ProblemDetail")
    void update_asAnon_returns401ProblemDetail() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API_ME_EVENTS.concat("/" + event.getId());
        var body = eventBody(factory.sevilla().getId().toString(), "[\"%s\"]".formatted(TestConstants.MOCK_ARTIST_NAME_AY));

        var ra = mockMvc.perform(put(api)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("PUT /api/me/events/{eventId} evento de otro usuario -> 403 ProblemDetail")
    void update_asUser_notOwner_returns403ProblemDetail() throws Exception {
        var event = factory.approvedEvent("Titulo", factory.sevilla(), "Sevilla", "Sala X", TestDates.tomorrow(),
                TestConstants.MOCK_MODERATOR_ID, TestConstants.MOCK_ARTIST_NAME_AY);
        String api = API_ME_EVENTS.concat("/" + event.getId());
        var body = eventBody(factory.sevilla().getId().toString(), "[\"%s\"]".formatted(TestConstants.MOCK_ARTIST_NAME_AY));

        var ra = mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("PUT /api/me/events/{eventId} no existe -> 404 ProblemDetail")
    void update_asOwner_notFound_returns404ProblemDetail() throws Exception {
        String api = API_ME_EVENTS.concat("/cccccccc-0000-0000-0000-000000000099");
        var body = eventBody(factory.sevilla().getId().toString(), "[\"%s\"]".formatted(TestConstants.MOCK_ARTIST_NAME_AY));

        var ra = mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNotFound());

        contractUtils.expectProblemDetail(ra, 404, api);
    }

    @Test
    @DisplayName("PUT /api/me/events/{eventId} estado no editable -> 409 ProblemDetail")
    void update_asOwner_notEditableStatus_returns409ProblemDetail() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = API_ME_EVENTS.concat("/" + event.getId());
        var body = eventBody(factory.sevilla().getId().toString(), "[\"%s\"]".formatted(TestConstants.MOCK_ARTIST_NAME_AY));

        var ra = mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }

    @Test
    @DisplayName("DELETE /api/me/events/{eventId} con auth -> 204 sin contenido")
    void delete_asOwner_returns204NoContent() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = API_ME_EVENTS.concat("/" + event.getId());

        mockMvc.perform(delete(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/me/events/{eventId} sin eventId -> 400 ProblemDetail")
    void delete_asOwner_missingEventId_returns400ProblemDetail() throws Exception {
        String api = API_ME_EVENTS.concat("/");

        var ra = mockMvc.perform(delete(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("DELETE /api/me/events/{eventId} sin auth -> 401 ProblemDetail")
    void delete_asAnon_returns401ProblemDetail() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API_ME_EVENTS.concat("/" + event.getId());

        var ra = mockMvc.perform(delete(api)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("DELETE /api/me/events/{eventId} evento de otro usuario -> 403 ProblemDetail")
    void delete_asUser_notOwner_returns403ProblemDetail() throws Exception {
        var event = factory.approvedEvent("Titulo", factory.sevilla(), "Sevilla", "Sala X", TestDates.tomorrow(),
                TestConstants.MOCK_MODERATOR_ID, TestConstants.MOCK_ARTIST_NAME_AY);
        String api = API_ME_EVENTS.concat("/" + event.getId());

        var ra = mockMvc.perform(delete(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("DELETE /api/me/events/{eventId} no existe -> 404 ProblemDetail")
    void delete_asOwner_notFound_returns404ProblemDetail() throws Exception {
        String api = API_ME_EVENTS.concat("/cccccccc-0000-0000-0000-000000000099");

        var ra = mockMvc.perform(delete(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        contractUtils.expectProblemDetail(ra, 404, api);
    }

    @Test
    @DisplayName("DELETE /api/me/events/{eventId} estado no eliminable (CANCELED) -> 409 ProblemDetail")
    void delete_asOwner_notErasableStatus_returns409ProblemDetail() throws Exception {
        var event = factory.canceledBarcelonaManifa();
        String api = API_ME_EVENTS.concat("/" + event.getId());

        var ra = mockMvc.perform(delete(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }

    @Test
    @DisplayName("DELETE /api/me/events/{eventId} estado APPROVED -> 409 ProblemDetail (contactar administración)")
    void delete_asOwner_approvedEvent_returns409ProblemDetail() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API_ME_EVENTS.concat("/" + event.getId());

        var ra = mockMvc.perform(delete(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }

    /*
     * Payload crear evento
     */
    private static String eventBody(String provinceId, String artists) {
        return """
                {
                  "title": "Concierto de prueba en Sevilla",
                  "description": "Evento creado desde test",
                  "startDateTime": "%s",
                  "venueName": "Sala Custom",
                  "provinceId": "%s",
                  "cityName": "Sevilla",
                  "artists": %s,
                  "sourceUrl": "https://example.com"
                }
                """.formatted(TestDates.genericFuture(), provinceId, artists);
    }
}
