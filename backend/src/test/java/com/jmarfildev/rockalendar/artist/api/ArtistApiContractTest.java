package com.jmarfildev.rockalendar.artist.api;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
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
    @Autowired
    DatabaseCleaner cleaner;

    @BeforeEach
    void setup() {
        cleaner.truncateMutableTables();
    }

    private final String apiModerationArtist = "/api/moderation/artists";
    private final String apiArtists = "/api/artists";
    private final String mockArtistNameCGPP = "Catalina Grande Piñón Pequeño";

    @Test
    @DisplayName("POST /api/moderation/artists como anonimo -> 401")
    void createArtist_asAnon_returns401() throws Exception {
        var ra = mockMvc.perform(post(apiModerationArtist).contentType(MediaType.APPLICATION_JSON)
                                                            .content("""
                                                                         { "name": "%s" }
                                                                     """.formatted(mockArtistNameCGPP)));

        contractUtils.expectProblemDetail(ra, 401, apiModerationArtist);
    }

    @Test
    @DisplayName("POST /api/moderation/artists como USER -> 403")
    void createArtist_asUser_returns403() throws Exception {
        var ra = mockMvc.perform(post(apiModerationArtist).with(contractUtils.authJwt())
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .content("""
                                                                         { "name": "%s" }
                                                                     """.formatted(mockArtistNameCGPP)));

        contractUtils.expectProblemDetail(ra, 403, apiModerationArtist);
    }

    @Test
    @DisplayName("POST /api/artists como MODERATOR -> 201 con artista creado")
    void createArtist_asModerator_returns201() throws Exception {
        mockMvc.perform(post(apiModerationArtist).with(contractUtils.authJwtModerator())
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content("""
                                                                { "name": "%s" }
                                                            """.formatted(mockArtistNameCGPP)))
               .andExpect(status().isCreated())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id", not(emptyOrNullString())))
               .andExpect(jsonPath("$.name").value(mockArtistNameCGPP));
    }

    @Test
    @DisplayName("POST /api/artists duplicado por slug -> 409 Conflict")
    void createArtist_duplicateSlug_returns409() throws Exception {
        // primera creación OK
        mockMvc.perform(post(apiModerationArtist).with(contractUtils.authJwtAdmin())
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content("""
                                                                { "name": "%s" }
                                                            """.formatted(TestConstants.MOCK_ARTIST_NAME_AY)))
               .andExpect(status().isCreated());

        // segunda creación repetida (por slug)
        var ra = mockMvc.perform(post(apiModerationArtist).with(contractUtils.authJwtAdmin())
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .content("""
                                                                        { "name": "AGAINST   YOU" }
                                                                     """));

        contractUtils.expectProblemDetail(ra, 409, apiModerationArtist);
    }

    @Test
    @DisplayName("POST /api/artists con name vacío -> 400 Validation ProblemDetail")
    void createArtist_blankName_returns400() throws Exception {
        var ra = mockMvc.perform(post(apiModerationArtist).with(contractUtils.authJwtAdmin())
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .content("""
                                                                         { "name": "   " }
                                                                     """));

        contractUtils.expectProblemDetail(ra, 400, apiModerationArtist);
    }

    @Test
    @DisplayName("POST /api/artists con name que normaliza a slug vacío -> 400 BadRequest ProblemDetail")
    void createArtist_slugBlank_returns400() throws Exception {
        var ra = mockMvc.perform(post(apiModerationArtist).with(contractUtils.authJwtAdmin())
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .content("""
                                                                         { "name": "!!!" }
                                                                     """));

        contractUtils.expectProblemDetail(ra, 400, apiModerationArtist);
    }

    @Test
    @DisplayName("GET /api/artists/{id} existente (público) -> 200 con id y name")
    void getById_existingArtist_returns200() throws Exception {
        var artist = factory.againstYou();

        mockMvc.perform(get(apiArtists + "/" + artist.getId()))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value(artist.getId().toString()))
               .andExpect(jsonPath("$.name").value(TestConstants.MOCK_ARTIST_NAME_AY));
    }

    @Test
    @DisplayName("GET /api/artists/{id} no existe -> 404 ProblemDetail")
    void getById_notFound_returns404ProblemDetail() throws Exception {
        String api = apiArtists + "/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

        var ra = mockMvc.perform(get(api))
                        .andExpect(status().isNotFound());

        contractUtils.expectProblemDetail(ra, 404, api);
    }

    @Test
    @DisplayName("GET /api/artists?query=metal (público) -> 200 lista")
    void searchArtists_public_returns200List() throws Exception {
        // Primero se crea
        mockMvc.perform(post(apiModerationArtist).with(contractUtils.authJwtAdmin())
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content("""
                                                                { "name": "%s" }
                                                            """.formatted(TestConstants.MOCK_ARTIST_NAME_AY)));

        // Después se consulta
        mockMvc.perform(get(apiArtists).param("query", "you"))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$", not(empty())))
               .andExpect(jsonPath("$[0].name", not(emptyOrNullString())));
    }
}
