package com.jmarfildev.rockalendar.artist.api;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;

/**
 * @author jmarfil
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
class ArtistApiContractTest extends AbstractPostgresTest {

    @Autowired
    TestDataFactory factory;
    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    MockMvc mockMvc;

    private final String API_ADMIN_ARTIST = "/api/admin/artists";
    private final String API_ARTISTS = "/api/artists";
    private final String MOCK_ARTIST_NAME_CGPP = "Catalina Grande Piñón Pequeño";

    @Test
    @DisplayName("POST /api/artists como MODERATOR -> 201 con artista creado")
    void createArtist_asModerator_returns201() throws Exception {
        mockMvc.perform(post(API_ADMIN_ARTIST)
                .with(contractUtils.authJwtModerator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "name": "%s" }
                        """.formatted(MOCK_ARTIST_NAME_CGPP)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", not(emptyOrNullString())))
                .andExpect(jsonPath("$.name").value(MOCK_ARTIST_NAME_CGPP))
                .andExpect(jsonPath("$.slug", not(emptyOrNullString())));
    }

    @Test
    @DisplayName("POST /api/artists duplicado por slug -> 409 Conflict")
    void createArtist_duplicateSlug_returns409() throws Exception {
        // primera creación OK
        mockMvc.perform(post(API_ADMIN_ARTIST)
                .with(contractUtils.authJwtAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "name": "%s" }
                        """.formatted(TestConstants.MOCK_ARTIST_NAME_AY)))
                .andExpect(status().isCreated());

        // segunda creación repetida (por slug)
        var ra = mockMvc.perform(post(API_ADMIN_ARTIST)
                .with(contractUtils.authJwtAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                           { "name": "AGAINST   YOU" }
                        """));

        contractUtils.expectProblemDetail(ra, 409, API_ADMIN_ARTIST);
    }

    @Test
    @DisplayName("POST /api/artists con name vacío -> 400 Validation ProblemDetail")
    void createArtist_blankName_returns400() throws Exception {
        var ra = mockMvc.perform(post(API_ADMIN_ARTIST)
                .with(contractUtils.authJwtAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "name": "   " }
                        """));

        contractUtils.expectProblemDetail(ra, 400, API_ADMIN_ARTIST);
    }

    @Test
    @DisplayName("POST /api/artists con name que normaliza a slug vacío -> 400 BadRequest ProblemDetail")
    void createArtist_slugBlank_returns400() throws Exception {
        var ra = mockMvc.perform(post(API_ADMIN_ARTIST)
                .with(contractUtils.authJwtAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "name": "!!!" }
                        """));

        contractUtils.expectProblemDetail(ra, 400, API_ADMIN_ARTIST);
    }

    @Test
    @DisplayName("GET /api/artists?query=metal (público) -> 200 lista")
    void searchArtists_public_returns200List() throws Exception {
        // Primero se crea
        mockMvc.perform(post(API_ADMIN_ARTIST)
                .with(contractUtils.authJwtAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            { "name": "%s" }
                        """.formatted(TestConstants.MOCK_ARTIST_NAME_AY)));

        // Después se consulta
        mockMvc.perform(get(API_ARTISTS).param("query", "you"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].name", not(emptyOrNullString())));
    }
}
