package com.jmarfildev.rockalendar.agenda.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.jmarfildev.rockalendar.agenda.domain.InteractionStatus;
import com.jmarfildev.rockalendar.agenda.domain.UserEvent;
import com.jmarfildev.rockalendar.agenda.domain.UserEventId;
import com.jmarfildev.rockalendar.agenda.persistence.UserEventRepository;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;

/**
 * @author jmarfil
 */
@SpringBootTest
@AutoConfigureMockMvc
class AgendaApiContractTest extends AbstractPostgresTest {

    @Autowired
    TestDataFactory factory;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    UserEventRepository userEventRepository;
    @Autowired
    MockMvc mockMvc;

    private final String API_AGENDA = "/api/me/agenda";

    @Test
    @DisplayName("GET /api/me/agenda sin auth -> 401 ProblemDetail")
    void getAgenda_asAnon_returns401() throws Exception {
        var ra = mockMvc.perform(get(API_AGENDA));
        contractUtils.expectProblemDetail(ra, 401, API_AGENDA);
    }

    @Test
    @DisplayName("GET /api/me/agenda con auth y agenda vacía -> 200 lista vacía")
    void getAgenda_asUser_emptyAgenda_returns200EmptyList() throws Exception {
        mockMvc.perform(get(API_AGENDA)
                .with(contractUtils.authJwt()))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/me/agenda con items -> 200 lista con datos del evento")
    void getAgenda_asUser_withItems_returns200WithEventData() throws Exception {
        Event event = factory.approvedMadridAgainstYou();
        UUID userId = UUID.fromString(TestConstants.MOCK_USER_ID);
        userEventRepository.save(UserEvent.builder()
                                          .id(new UserEventId(userId, event.getId()))
                                          .status(InteractionStatus.GOING)
                                          .build());

        mockMvc.perform(get(API_AGENDA)
                .with(contractUtils.authJwt()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].eventId").value(event.getId().toString()))
               .andExpect(jsonPath("$[0].title").value(event.getTitle()))
               .andExpect(jsonPath("$[0].status").value(InteractionStatus.GOING.name()));
    }

    @Test
    @DisplayName("PUT /api/me/agenda/{eventId} sin auth -> 401 ProblemDetail")
    void upsert_asAnon_returns401() throws Exception {
        String api = API_AGENDA + "/" + UUID.randomUUID();
        var ra = mockMvc.perform(put(api)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"INTERESTED\"}"));
        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("PUT /api/me/agenda/{eventId} con auth y evento APPROVED -> 200 AgendaItemDto")
    void upsert_asUser_approvedEvent_returns200() throws Exception {
        Event event = factory.approvedMadridAgainstYou();
        String api = API_AGENDA + "/" + event.getId();

        mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"INTERESTED\"}"))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.eventId").value(event.getId().toString()))
               .andExpect(jsonPath("$.status").value(InteractionStatus.INTERESTED.name()))
               .andExpect(jsonPath("$.title").isNotEmpty());
    }

    @Test
    @DisplayName("PUT /api/me/agenda/{eventId} actualizar interacción existente -> 200 con nuevo status")
    void upsert_asUser_updateExisting_returns200WithNewStatus() throws Exception {
        Event event = factory.approvedBarcelonaBoikot();
        UUID userId = UUID.fromString(TestConstants.MOCK_USER_ID);
        userEventRepository.save(UserEvent.builder()
                                          .id(new UserEventId(userId, event.getId()))
                                          .status(InteractionStatus.INTERESTED)
                                          .build());

        String api = API_AGENDA + "/" + event.getId();
        mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"GOING\"}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value(InteractionStatus.GOING.name()));
    }

    @Test
    @DisplayName("PUT /api/me/agenda/{eventId} evento no APPROVED -> 409 ProblemDetail")
    void upsert_asUser_notApprovedEvent_returns409() throws Exception {
        Event event = factory.pendingMadridAgainstYou();
        String api = API_AGENDA + "/" + event.getId();

        var ra = mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"INTERESTED\"}"));

        contractUtils.expectProblemDetail(ra, 409, api);
    }

    @Test
    @DisplayName("PUT /api/me/agenda/{eventId} evento no existe -> 404 ProblemDetail")
    void upsert_asUser_nonExistentEvent_returns404() throws Exception {
        String api = API_AGENDA + "/" + UUID.randomUUID();

        var ra = mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"INTERESTED\"}"));

        contractUtils.expectProblemDetail(ra, 404, api);
    }

    @Test
    @DisplayName("PUT /api/me/agenda/{eventId} sin body -> 400 ProblemDetail")
    void upsert_asUser_noBody_returns400() throws Exception {
        String api = API_AGENDA + "/" + UUID.randomUUID();

        var ra = mockMvc.perform(put(api)
                .with(contractUtils.authJwt())
                .contentType(MediaType.APPLICATION_JSON));

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("DELETE /api/me/agenda/{eventId} sin auth -> 401 ProblemDetail")
    void remove_asAnon_returns401() throws Exception {
        String api = API_AGENDA + "/" + UUID.randomUUID();
        var ra = mockMvc.perform(delete(api));
        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("DELETE /api/me/agenda/{eventId} con auth -> 204 sin contenido")
    void remove_asUser_existingInteraction_returns204() throws Exception {
        Event event = factory.approvedValenciaPast();
        UUID userId = UUID.fromString(TestConstants.MOCK_USER_ID);
        userEventRepository.save(UserEvent.builder()
                                          .id(new UserEventId(userId, event.getId()))
                                          .status(InteractionStatus.INTERESTED)
                                          .build());

        String api = API_AGENDA + "/" + event.getId();
        mockMvc.perform(delete(api).with(contractUtils.authJwt()))
               .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/me/agenda/{eventId} interacción no existe -> 204 (idempotente)")
    void remove_asUser_noInteraction_returns204() throws Exception {
        String api = API_AGENDA + "/" + UUID.randomUUID();
        mockMvc.perform(delete(api).with(contractUtils.authJwt()))
               .andExpect(status().isNoContent());
    }
}
