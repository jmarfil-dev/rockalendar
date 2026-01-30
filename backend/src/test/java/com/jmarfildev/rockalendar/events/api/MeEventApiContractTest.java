package com.jmarfildev.rockalendar.events.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;

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
    @Autowired
    ObjectMapper objectMapper;

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
    @DisplayName("POST /api/events sin auth -> 401 ProblemDetail")
    void postEvents_withoutAuth_returns401ProblemDetail() throws Exception {
        String artists = "[%s]".formatted(TestConstants.MOCK_ARTIST_NAME_AY);
        var body = proposeEventBody(factory.sevilla().getId().toString(), artists);

        var ra = mockMvc.perform(post(API_EVENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        contractUtils.expectProblemDetail(ra, 401, API_EVENTS);
    }

    @Test
    @DisplayName("POST /api/events con auth pero payload inválido -> 400 ProblemDetail")
    void postEvents_withAuth_invalidPayload_returns400ProblemDetail() throws Exception {
        // Error artists vacío
        var body = proposeEventBody(factory.sevilla().getId().toString(), "[]");

        var ra = mockMvc.perform(post(API_EVENTS)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        contractUtils.expectProblemDetail(ra, 400, API_EVENTS);
    }

    /*
     * Payload crear evento
     */
    private static String proposeEventBody(String provinceId, String artists) {
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
                """.formatted(TestConstants.GENERIC_DATE, provinceId, artists);
    }
}
