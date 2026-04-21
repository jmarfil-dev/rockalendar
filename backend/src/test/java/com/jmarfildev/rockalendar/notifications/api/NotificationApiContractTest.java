package com.jmarfildev.rockalendar.notifications.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.notifications.domain.NotificationType;
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;

/**
 * @author jmarfil
 */
@SpringBootTest
@AutoConfigureMockMvc
class NotificationApiContractTest extends AbstractPostgresTest {

    private static final String BASE = "/api/notifications";
    private static final UUID USER_ID = UUID.fromString(TestConstants.MOCK_USER_ID);

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    TestDataFactory factory;
    @Autowired
    DatabaseCleaner cleaner;

    @BeforeEach
    void cleanDb() {
        cleaner.truncateMutableTables();
    }

    // -------------------------------------------------------------------------
    // GET /api/notifications
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/notifications sin autenticar -> 401")
    void list_asAnon_401() throws Exception {
        var ra = mockMvc.perform(get(BASE))
                        .andExpect(status().isUnauthorized());
        contractUtils.expectProblemDetail(ra, 401, BASE);
    }

    @Test
    @DisplayName("GET /api/notifications con auth, sin notificaciones -> 200 vacío")
    void list_withAuth_empty_200() throws Exception {
        mockMvc.perform(get(BASE).with(contractUtils.authJwt()))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/notifications con auth -> 200 devuelve notificaciones del usuario")
    void list_withAuth_returnsUserNotifications() throws Exception {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);
        factory.notification(USER_ID, NotificationType.EVENT_REJECTED, null, true);
        // notificación de otro usuario: no debe aparecer
        factory.notification(UUID.fromString(TestConstants.MOCK_MODERATOR_ID), NotificationType.EVENT_APPROVED, null, false);

        mockMvc.perform(get(BASE).with(contractUtils.authJwt()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content", hasSize(2)))
               .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/notifications?types=EVENT_APPROVED -> 200 filtrado por tipo")
    void list_withTypeFilter_returnOnlyMatchingType() throws Exception {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);
        factory.notification(USER_ID, NotificationType.EVENT_REJECTED, null, false);

        mockMvc.perform(get(BASE).param("types", "EVENT_APPROVED").with(contractUtils.authJwt()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content", hasSize(1)))
               .andExpect(jsonPath("$.content[0].type").value("EVENT_APPROVED"));
    }

    @Test
    @DisplayName("GET /api/notifications?bandeja=USER -> 200 filtrado por bandeja")
    void list_withBandejaFilter_returnsOnlyBandejaNotifications() throws Exception {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);          // USER
        factory.notification(USER_ID, NotificationType.EVENT_PENDING_MODERATION, null, false); // MODERATION

        mockMvc.perform(get(BASE).param("bandeja", "USER").with(contractUtils.authJwt()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content", hasSize(1)))
               .andExpect(jsonPath("$.content[0].type").value("EVENT_APPROVED"));
    }

    @Test
    @DisplayName("GET /api/notifications?bandeja=USER&types=EVENT_PENDING_MODERATION -> bandeja tiene precedencia")
    void list_bandejaAndTypes_bandejaWins() throws Exception {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);          // USER
        factory.notification(USER_ID, NotificationType.EVENT_PENDING_MODERATION, null, false); // MODERATION

        mockMvc.perform(get(BASE).param("bandeja", "USER").param("types", "EVENT_PENDING_MODERATION").with(contractUtils.authJwt()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content", hasSize(1)))
               .andExpect(jsonPath("$.content[0].type").value("EVENT_APPROVED"));
    }

    // -------------------------------------------------------------------------
    // GET /api/notifications/unread-count
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/notifications/unread-count sin autenticar -> 401")
    void unreadCount_asAnon_401() throws Exception {
        String api = BASE + "/unread-count";
        var ra = mockMvc.perform(get(api))
                        .andExpect(status().isUnauthorized());
        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count con auth -> 200 devuelve conteo por bandeja")
    void unreadCount_withAuth_returnsCountPerBandeja() throws Exception {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);       // user
        factory.notification(USER_ID, NotificationType.EVENT_REJECTED, null, false);       // user
        factory.notification(USER_ID, NotificationType.EVENT_NEEDS_CHANGES, null, true);   // user, leída
        factory.notification(USER_ID, NotificationType.EVENT_PENDING_MODERATION, null, false); // moderation

        mockMvc.perform(get(BASE + "/unread-count").with(contractUtils.authJwt()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.user").value(2))
               .andExpect(jsonPath("$.moderation").value(1))
               .andExpect(jsonPath("$.admin").value(0));
    }

    // -------------------------------------------------------------------------
    // POST /api/notifications/{id}/read
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/notifications/{id}/read sin autenticar -> 401")
    void markAsRead_asAnon_401() throws Exception {
        String api = BASE + "/" + UUID.randomUUID() + "/read";
        var ra = mockMvc.perform(post(api))
                        .andExpect(status().isUnauthorized());
        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/read como dueño -> 204")
    void markAsRead_asOwner_204() throws Exception {
        var notif = factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);
        String api = BASE + "/" + notif.getId() + "/read";

        mockMvc.perform(post(api).with(contractUtils.authJwt()))
               .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/read como otro usuario -> 403")
    void markAsRead_asOtherUser_403() throws Exception {
        var notif = factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);
        String api = BASE + "/" + notif.getId() + "/read";

        var ra = mockMvc.perform(post(api).with(contractUtils.authJwtModerator()))
                        .andExpect(status().isForbidden());
        contractUtils.expectProblemDetail(ra, 403, api);
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/read notificación no existe -> 404")
    void markAsRead_notFound_404() throws Exception {
        String api = BASE + "/" + UUID.randomUUID() + "/read";

        var ra = mockMvc.perform(post(api).with(contractUtils.authJwt()))
                        .andExpect(status().isNotFound());
        contractUtils.expectProblemDetail(ra, 404, api);
    }

    // -------------------------------------------------------------------------
    // POST /api/notifications/read-all
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/notifications/read-all sin autenticar -> 401")
    void markAllAsRead_asAnon_401() throws Exception {
        String api = BASE + "/read-all";
        var ra = mockMvc.perform(post(api))
                        .andExpect(status().isUnauthorized());
        contractUtils.expectProblemDetail(ra, 401, api);
    }

    @Test
    @DisplayName("POST /api/notifications/read-all con auth -> 204")
    void markAllAsRead_withAuth_204() throws Exception {
        factory.notification(USER_ID, NotificationType.EVENT_APPROVED, null, false);
        factory.notification(USER_ID, NotificationType.EVENT_REJECTED, null, false);

        mockMvc.perform(post(BASE + "/read-all").with(contractUtils.authJwt()))
               .andExpect(status().isNoContent());
    }
}
