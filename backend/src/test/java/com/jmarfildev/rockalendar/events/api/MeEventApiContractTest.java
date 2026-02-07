package com.jmarfildev.rockalendar.events.api;

import static org.hamcrest.Matchers.not;
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
    private final String API_EVENTS = "/api/events";

    @Test
    @DisplayName("GET /api/me/events sin auth -> 401 ProblemDetail")
    void getMeEvents_withoutAuth_returns401ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(API_ME_EVENTS));
        contractUtils.expectProblemDetail(ra, 401, API_ME_EVENTS);
    }

    @Test
    @DisplayName("GET /api/me/events con auth -> 200 (lista vacía OK)")
    void getMeEvents_withAuth_returns200() throws Exception {
        mockMvc.perform(get(API_ME_EVENTS).with(contractUtils.authJwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST " + API_EVENTS + " con auth -> 201 EventPrivateDto con status PENDING_MODERATION")
    void propose_withAuth_ok_201ReturnsEvent() throws Exception {
        var body = eventBody(factory.sevilla().getId().toString(), "[\"%s\"]".formatted(TestConstants.MOCK_ARTIST_NAME_AY));

        mockMvc.perform(post(API_EVENTS)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value(EventStatus.PENDING_MODERATION.name()))
                .andExpect(jsonPath("$.title").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/events sin auth -> 401 ProblemDetail")
    void postEvents_withoutAuth_returns401ProblemDetail() throws Exception {
        String artists = "[%s]".formatted(TestConstants.MOCK_ARTIST_NAME_AY);
        var body = eventBody(factory.sevilla().getId().toString(), artists);

        var ra = mockMvc.perform(post(API_EVENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        contractUtils.expectProblemDetail(ra, 401, API_EVENTS);
    }

    @Test
    @DisplayName("POST /api/events con auth pero payload inválido -> 400 ProblemDetail")
    void postEvents_withAuth_invalidPayload_returns400ProblemDetail() throws Exception {
        // Error artists vacío
        var body = eventBody(factory.sevilla().getId().toString(), "[]");

        var ra = mockMvc.perform(post(API_EVENTS)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        contractUtils.expectProblemDetail(ra, 400, API_EVENTS);
    }

    @Test
    @DisplayName("PUT " + API_ME_EVENTS + "/{eventId} con auth -> 200 EventPrivateDto con status PENDING_MODERATION")
    void update_withAuth_ok_200ReturnsEvent() throws Exception {
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
    @DisplayName("PUT " + API_ME_EVENTS + "/{eventId} con auth sin body -> 400")
    void update_withAuth_noRequest_400ProblemDetail() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API_ME_EVENTS.concat("/" + event.getId());

        var ra = mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("PUT " + API_ME_EVENTS + "/{eventId} endDateTime < startDateTime -> 400")
    void update_invalidDateRange_400ProblemDetail() throws Exception {
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
    @DisplayName("PUT " + API_ME_EVENTS + "/{eventId} sin auth -> 401")
    void update_withoutAuth_returns401ProblemDetail() throws Exception {
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
    @DisplayName("PUT " + API_ME_EVENTS + "/{eventId} evento de otro usuario -> 403")
    void update_notOwner_403ProblemDetail() throws Exception {
        var event = factory.approvedEvent("Titulo", factory.sevilla(), "Sevilla", "Sala X", TestDates.tomorrow(),
                TestConstants.MOCK_MODERATOR_ID, TestConstants.MOCK_ARTIST_NAME_AY); // Otro usuario lo crea
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
    @DisplayName("PUT " + API_ME_EVENTS + "/{eventId} no existe -> 404")
    void update_missingEvent_404ProblemDetail() throws Exception {
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
    @DisplayName("PUT " + API_ME_EVENTS + "/{eventId} estado no editable -> 409")
    void update_notEditableStatus_409ProblemDetail() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = API_ME_EVENTS.concat("/" + event.getId());
        var body = eventBody(factory.sevilla().getId().toString(), "[\"%s\"]".formatted(TestConstants.MOCK_ARTIST_NAME_AY));

        var ra = mockMvc.perform(put(api)
                .with(contractUtils.authJwt()) // debe ser owner
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
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
