package com.jmarfildev.rockalendar.events.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.jmarfildev.rockalendar.artists.domain.Artist;
import com.jmarfildev.rockalendar.artists.persistence.ArtistRepository;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.SlugNormalizer;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.events.api.dto.ProposeEventRequest;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;
import com.jmarfildev.rockalendar.support.TestDates;

/**
 * @author jmarfil
 *
 */
@DataJpaTest
@Import({ EventCommandService.class, DatabaseCleaner.class, TestDataFactory.class })
class EventCommandServiceTest extends AbstractPostgresTest {

    @Autowired
    EventCommandService service;
    @Autowired
    DatabaseCleaner cleaner;
    @Autowired
    TestDataFactory factory;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    ArtistRepository artistRepository;

    @MockitoBean
    CurrentUser currentUser;

    private final String MOCK_TITLE = "Concierto";
    private final String MOCK_WIZINK = "WiZink Center";

    @BeforeEach
    void setUp() {
        cleaner.truncateMutableTables();
        when(currentUser.userId())
                .thenReturn(UUID.fromString(TestConstants.MOCK_USER_ID));
    }

    @Test
    @DisplayName("propose: endDateTime < startDateTime -> 400 INVALID_EVENT_DATE")
    void propose_invalidEndBeforeStart_throws() {
        var req = new ProposeEventRequest(
                MOCK_TITLE,
                "Desc",
                TestDates.rangeEnd(),
                TestDates.rangeStart(),
                MOCK_WIZINK,
                factory.madrid().getId(),
                TestConstants.MADRID,
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.propose(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorMessages.INVALID_EVENT_DATE);
    }

    @Test
    @DisplayName("propose: artists vacíos tras normalizar -> 400 ARTIST_REQUIRED")
    void propose_artistsBecomeEmptyAfterNormalize_throws() {
        var req = new ProposeEventRequest(
                MOCK_TITLE,
                null,
                TestDates.madrid(),
                null,
                MOCK_WIZINK,
                factory.madrid().getId(),
                TestConstants.MADRID,
                List.of("   ", "!!!", "´´´"),
                null);

        assertThatThrownBy(() -> service.propose(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorMessages.ARTIST_REQUIRED);
    }

    @Test
    @DisplayName("propose: cityName se queda vacío tras normalizar -> 400 CITY_REQUIRED")
    void propose_cityBecomesBlankAfterNormalize_throws() {
        var req = new ProposeEventRequest(
                MOCK_TITLE,
                null,
                TestDates.madrid(),
                null,
                MOCK_WIZINK,
                factory.madrid().getId(),
                "!!!",
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.propose(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorMessages.CITY_REQUIRED);
    }

    @Test
    @DisplayName("propose: venueName se queda vacío tras normalizar -> 400 VENUE_REQUIRED")
    void propose_venueBecomesBlankAfterNormalize_throws() {
        var req = new ProposeEventRequest(
                MOCK_TITLE,
                null,
                TestDates.madrid(),
                null,
                "***",
                factory.madrid().getId(),
                TestConstants.MADRID,
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.propose(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorMessages.VENUE_REQUIRED);
    }

    @Test
    @DisplayName("propose: title se queda vacío tras normalizar -> 400 TITLE_REQUIRED")
    void propose_titleBecomesBlankAfterNormalize_throws() {
        var req = new ProposeEventRequest(
                "!!!",
                null,
                TestDates.madrid(),
                null,
                MOCK_WIZINK,
                factory.madrid().getId(),
                TestConstants.MADRID,
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.propose(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorMessages.TITLE_REQUIRED);
    }

    @Test
    @DisplayName("propose: provinceId inválida -> 400 INVALID_PROVINCE")
    void propose_invalidProvince_throws() {
        UUID missingProv = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        var req = new ProposeEventRequest(
                MOCK_TITLE,
                null,
                TestDates.madrid(),
                null,
                MOCK_WIZINK,
                missingProv,
                TestConstants.MADRID,
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.propose(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorMessages.INVALID_PROVINCE);
    }

    @Test
    @DisplayName("propose: crea evento PENDING_MODERATION con slugs normalizados y createdByUserId")
    void propose_createsPendingEvent_withNormalizedSlugs() {
        var req = new ProposeEventRequest(
                "  Ska-P en Madrid  ",
                "  descripcion  ",
                TestDates.madrid(),
                null,
                "  WiZink Center  ",
                factory.madrid().getId(),
                "  Madrid  ",
                List.of("  Ska-P  "),
                "  https://example.com  ");

        var saved = service.propose(req);

        var reloaded = eventRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.PENDING_MODERATION);
        assertThat(reloaded.getCreatedByUserId()).isEqualTo(UUID.fromString(TestConstants.MOCK_USER_ID));

        assertThat(reloaded.getTitle()).isEqualTo("Ska-P en Madrid");
        assertThat(reloaded.getCityName()).isEqualTo(TestConstants.MADRID);
        assertThat(reloaded.getVenueName()).isEqualTo(MOCK_WIZINK);

        assertThat(reloaded.getCitySlug()).isEqualTo(SlugNormalizer.of(TestConstants.MADRID));
        assertThat(reloaded.getVenueSlug()).isEqualTo(SlugNormalizer.of(MOCK_WIZINK));

        assertThat(reloaded.getArtists()).hasSize(1);
        assertThat(reloaded.getArtists().iterator().next().getSlug()).isEqualTo(SlugNormalizer.of("Ska-P"));
    }

    @Test
    @DisplayName("propose: reusa artista existente por slug (no crea duplicados)")
    void propose_reusesExistingArtistBySlug() {
        var existing = factory.againstYou();
        long before = artistRepository.count();

        var req = new ProposeEventRequest(
                "Against You en Madrid",
                null,
                TestDates.madrid(),
                null,
                MOCK_WIZINK,
                factory.madrid().getId(),
                TestConstants.MADRID,
                List.of("  AgAinST- yOU "),
                null);

        var saved = service.propose(req);

        var reloaded = eventRepository.findById(saved.getId()).orElseThrow();

        assertThat(artistRepository.count()).isEqualTo(before); // No crece count porque no vuelve a crear mismo artista
        assertThat(reloaded.getArtists()).singleElement().satisfies(a -> assertThat(a.getId()).isEqualTo(existing.getId()));
    }

    @Test
    @DisplayName("propose: artistas se deduplican por slug y preservan orden (LinkedHashSet)")
    void propose_deduplicatesArtists_preservesOrder() {
        var req = new ProposeEventRequest(
                MOCK_TITLE,
                null,
                TestDates.madrid(),
                null,
                MOCK_WIZINK,
                factory.madrid().getId(),
                TestConstants.MADRID,
                List.of("Ska-P", "  ska p  ", "Boikot"),
                null);

        var saved = service.propose(req);

        var reloaded = eventRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getArtists())
                .extracting(Artist::getSlug)
                .containsExactly("ska p", "boikot");

        assertThat(artistRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("propose: description y sourceUrl en blanco se guardan como null")
    void propose_blankOptionalFields_becomeNull() {
        var req = new ProposeEventRequest(
                MOCK_TITLE,
                "    ",
                TestDates.madrid(),
                null,
                MOCK_WIZINK,
                factory.madrid().getId(),
                TestConstants.MADRID,
                List.of("Ska-P"),
                "   ");

        var saved = service.propose(req);

        var reloaded = eventRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getDescription()).isNull();
        assertThat(reloaded.getSourceUrl()).isNull();
    }
}
