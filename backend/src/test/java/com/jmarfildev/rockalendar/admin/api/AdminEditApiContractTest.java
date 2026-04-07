package com.jmarfildev.rockalendar.admin.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.jmarfildev.rockalendar.common.storage.StorageService;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;
import com.jmarfildev.rockalendar.support.TestDates;

/**
 * @author jmarfil
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminEditApiContractTest extends AbstractPostgresTest {

    @Autowired
    TestDataFactory factory;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    StorageService storageService;

    private static final String API = "/api/admin/events/%s";

    @Test
    @DisplayName("PUT " + API + " como ADMIN, evento PENDING -> 200 sin cambio de estado")
    void edit_asAdmin_pendingEvent_200() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = API.formatted(event.getId());

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, api)
                .file(eventPart(factory.madrid().getIneCode()))
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.status").value(EventStatus.PENDING_MODERATION.name()))
                .andExpect(jsonPath("$.title").value("Nuevo título admin"));
    }

    @Test
    @DisplayName("PUT " + API + " como ADMIN, evento APPROVED -> 200")
    void edit_asAdmin_approvedEvent_200() throws Exception {
        var event = factory.approvedMadridAgainstYou();
        String api = API.formatted(event.getId());

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, api)
                .file(eventPart(factory.madrid().getIneCode()))
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EventStatus.APPROVED.name()));
    }

    @Test
    @DisplayName("PUT " + API + " como ADMIN, evento NEEDS_CHANGES -> 200")
    void edit_asAdmin_needsChangesEvent_200() throws Exception {
        var event = factory.needsChangesMadridAgainstYou();
        String api = API.formatted(event.getId());

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, api)
                .file(eventPart(factory.madrid().getIneCode()))
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EventStatus.NEEDS_CHANGES.name()));
    }

    @Test
    @DisplayName("PUT " + API + " sin parte event -> 400")
    void edit_asAdmin_noBody_400() throws Exception {
        var event = factory.pendingMadridAgainstYou();
        String api = API.formatted(event.getId());

        var ra = mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, api)
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isBadRequest());

        contractUtils.expectProblemDetail(ra, 400, api);
    }

    @Test
    @DisplayName("PUT " + API + " sin autenticar -> 401")
    void edit_asAnon_401() throws Exception {
        String api = API.formatted(UUID.randomUUID());

        var ra = mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, api)
                .file(eventPart((short) 28)))
                .andExpect(status().isUnauthorized());

        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("PUT " + API + " como USER -> 403")
    void edit_asUser_403() throws Exception {
        String api = API.formatted(UUID.randomUUID());

        var ra = mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, api)
                .file(eventPart((short) 28))
                .with(contractUtils.authJwt()))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("PUT " + API + " como MODERATOR -> 403")
    void edit_asModerator_403() throws Exception {
        String api = API.formatted(UUID.randomUUID());

        var ra = mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, api)
                .file(eventPart((short) 28))
                .with(contractUtils.authJwtModerator()))
                .andExpect(status().isForbidden());

        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("PUT " + API + " evento no existe -> 404")
    void edit_asAdmin_notFound_404() throws Exception {
        String api = API.formatted(UUID.randomUUID());

        var ra = mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, api)
                .file(eventPart((short) 28))
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isNotFound());

        contractUtils.expectProblemDetail(ra, 404, api);
    }

    @Test
    @DisplayName("PUT " + API + " estado no editable (REJECTED) -> 409")
    void edit_asAdmin_wrongStatus_409() throws Exception {
        var event = factory.rejectedValenciaMafalda();
        String api = API.formatted(event.getId());

        var ra = mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, api)
                .file(eventPart(factory.valencia().getIneCode()))
                .with(contractUtils.authJwtAdmin()))
                .andExpect(status().isConflict());

        contractUtils.expectProblemDetail(ra, 409, api);
    }

    /*
     * Helpers
     */

    private static MockMultipartFile eventPart(short provinceId) {
        var dt = TestDates.genericFuture();
        String json = """
                {
                  "title": "Nuevo título admin",
                  "description": "Descripción editada por admin",
                  "startDate": "%s",
                  "startTime": "%s",
                  "venueName": "Sala Admin",
                  "provinceId": %s,
                  "cityName": "Madrid",
                  "artists": ["%s"],
                  "sourceUrl": "https://example.com"
                }
                """.formatted(dt.toLocalDate(), dt.toLocalTime().withNano(0), provinceId, TestConstants.MOCK_ARTIST_NAME_AY);
        return new MockMultipartFile("event", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes(StandardCharsets.UTF_8));
    }
}
