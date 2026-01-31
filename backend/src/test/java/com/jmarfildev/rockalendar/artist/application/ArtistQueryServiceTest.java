package com.jmarfildev.rockalendar.artist.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.artists.application.ArtistQueryService;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;

/**
 * @author jmarfil
 *
 */
@DataJpaTest
@Import({ ArtistQueryService.class, DatabaseCleaner.class, TestDataFactory.class })
public class ArtistQueryServiceTest extends AbstractPostgresTest {

    @Autowired
    ArtistQueryService service;
    @Autowired
    DatabaseCleaner cleaner;
    @Autowired
    TestDataFactory factory;

    private final String MOCK_ARTIST_NAME_SKAP = "Ska-P";

    @BeforeEach
    void cleanDb() {
        cleaner.truncateMutableTables();
    }

    @Test
    @DisplayName("searchArtistsAutocomplete: trim() aplicado y devuelve coincidencias")
    void autocomplete_trimsQuery_andReturnsMatches() {
        factory.againstYou();
        factory.laPolla();
        flush();

        var result = service.searchArtistsAutocomplete("  aga  ");

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(ArtistDto::name).anyMatch(TestConstants.MOCK_ARTIST_NAME_AY::equals);
    }

    @Test
    @DisplayName("searchArtistsAutocomplete: encuentra por slug normalizado (ska-p -> ska p)")
    void autocomplete_matchesBySlug() {
        factory.artist(MOCK_ARTIST_NAME_SKAP);
        flush();

        var result = service.searchArtistsAutocomplete("ska-p");

        assertThat(result).extracting(ArtistDto::name).anyMatch(MOCK_ARTIST_NAME_SKAP::equals);
    }

    @Test
    @DisplayName("searchArtistsAutocomplete: máximo 10 resultados")
    void autocomplete_limitsToTop10() {
        // Crea 25 artistas en base de datos
        String[] names = IntStream.range(0, 25)
                .mapToObj(i -> "Artist " + i)
                .toArray(String[]::new);
        factory.artists(names);
        flush();

        var result = service.searchArtistsAutocomplete("artist");

        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("searchArtistsAutocomplete: no revienta con mayúsculas/minúsculas")
    void autocomplete_isCaseInsensitive() {
        factory.artist(MOCK_ARTIST_NAME_SKAP);
        flush();

        var resultUpper = service.searchArtistsAutocomplete("SKA");
        var resultLower = service.searchArtistsAutocomplete("ska");

        assertThat(resultUpper).extracting(ArtistDto::name).anyMatch(MOCK_ARTIST_NAME_SKAP::equals);
        assertThat(resultLower).extracting(ArtistDto::name).anyMatch(MOCK_ARTIST_NAME_SKAP::equals);
    }

    @Test
    @DisplayName("searchArtistsAutocomplete: query en blanco -> devuelve vacío (o lanza) según contrato")
    void autocomplete_blankQuery_behavior() {
        var result = service.searchArtistsAutocomplete("   ");

        assertThat(result).isEmpty();
    }
}
