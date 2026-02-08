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
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.common.error.ForbiddenException;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.SlugNormalizer;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapperImpl;
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
@Import({ EventCommandService.class, DatabaseCleaner.class, TestDataFactory.class, EventMapperImpl.class })
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
    @DisplayName("propose: invalid date range -> BadRequestException")
    void propose_invalidDateRange_throws() {
        var req = new SubmitEventRequest(
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
    @DisplayName("propose: artists vacíos tras normalizar -> BadRequestException")
    void propose_artistsBecomeEmptyAfterNormalize_throws() {
        var req = new SubmitEventRequest(
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
    @DisplayName("propose: citySlug se queda vacío tras normalizar -> BadRequestException")
    void propose_cityBecomesBlankAfterNormalize_throws() {
        var req = new SubmitEventRequest(
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
    @DisplayName("propose: venueSlug se queda vacío tras normalizar -> BadRequestException")
    void propose_venueBecomesBlankAfterNormalize_throws() {
        var req = new SubmitEventRequest(
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
    @DisplayName("propose: title se queda vacío tras normalizar -> BadRequestException")
    void propose_titleBecomesBlankAfterNormalize_throws() {
        var req = new SubmitEventRequest(
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
    @DisplayName("propose: provinceId inválida -> BadRequestException")
    void propose_invalidProvince_throws() {
        UUID missingProv = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        var req = new SubmitEventRequest(
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
    @DisplayName("propose: ok -> crea evento PENDING_MODERATION")
    void propose_ok_createPendingEvent() {
        var req = new SubmitEventRequest(
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

        var reloaded = eventRepository.findById(saved.id()).orElseThrow();

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
    void propose_reusesExistingArtistBySlug_createEvent() {
        var existing = factory.againstYou();
        long before = artistRepository.count();

        var req = new SubmitEventRequest(
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

        var reloaded = eventRepository.findById(saved.id()).orElseThrow();

        assertThat(artistRepository.count()).isEqualTo(before); // No crece count porque no vuelve a crear mismo artista
        assertThat(reloaded.getArtists()).singleElement().satisfies(a -> assertThat(a.getId()).isEqualTo(existing.getId()));
    }

    @Test
    @DisplayName("propose: artistas  no se duplican por slug y preservan orden (LinkedHashSet)")
    void propose_deduplicatesArtists_preservesOrder() {
        var req = new SubmitEventRequest(
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

        var reloaded = eventRepository.findById(saved.id()).orElseThrow();
        assertThat(reloaded.getArtists())
                .extracting(Artist::getSlug)
                .containsExactly("ska p", "boikot");

        assertThat(artistRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("propose: description y sourceUrl en blanco se guardan como null")
    void propose_blankOptionalFields_becomeNull() {
        var req = new SubmitEventRequest(
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

        var reloaded = eventRepository.findById(saved.id()).orElseThrow();
        assertThat(reloaded.getDescription()).isNull();
        assertThat(reloaded.getSourceUrl()).isNull();
    }

    @Test
    @DisplayName("update: ok -> actualiza evento PENDING_MODERATION")
    void update_ok_updatePendingEvent() {
        var event = factory.approvedMadridAgainstYou();
        var req = new SubmitEventRequest(
                "  Against You en concierto  ",
                "  descripcion  ",
                TestDates.madrid(),
                null,
                "  WiZink Center  ",
                factory.madrid().getId(),
                "  Madrid  ",
                List.of("  Against You  "),
                "  https://example.com  ");

        var updated = service.update(event.getId(), req);

        var reloaded = eventRepository.findById(updated.id()).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.PENDING_MODERATION);
        assertThat(reloaded.getTitle()).isEqualTo("Against You en concierto");
        assertThat(reloaded.getVenueName()).isEqualTo(MOCK_WIZINK);
        assertThat(reloaded.getVenueSlug()).isEqualTo(SlugNormalizer.of(MOCK_WIZINK));
        assertThat(reloaded.getArtists()).hasSize(1);
        assertThat(reloaded.getArtists().iterator().next().getSlug()).isEqualTo(SlugNormalizer.of(TestConstants.MOCK_ARTIST_NAME_AY));
    }

    @Test
    @DisplayName("update: evento no existe -> NotFoundException")
    void update_missingEvent_throws() {
        var req = new SubmitEventRequest(
                MOCK_TITLE,
                "Desc",
                TestDates.rangeStart(),
                TestDates.rangeEnd(),
                MOCK_WIZINK,
                factory.madrid().getId(),
                TestConstants.MADRID,
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.update(UUID.fromString("cccccccc-0000-0000-0000-000000000099"), req))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorMessages.EVENT_NOT_FOUND);
    }

    @Test
    @DisplayName("update: evento no pertenece al usuario -> ForbiddenException")
    void update_notOwner_throws() {
        var event = factory.approvedEvent("Titulo", factory.sevilla(), "Sevilla", "Sala X", TestDates.tomorrow(),
                TestConstants.MOCK_MODERATOR_ID, TestConstants.MOCK_ARTIST_NAME_AY); // Otro usuario lo crea
        var req = new SubmitEventRequest(
                MOCK_TITLE,
                "Desc",
                TestDates.rangeStart(),
                TestDates.rangeEnd(),
                MOCK_WIZINK,
                factory.madrid().getId(),
                TestConstants.MADRID,
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.update(event.getId(), req))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ErrorMessages.EVENT_NOT_OWNER);
    }

    @Test
    @DisplayName("update: evento en estado no editable -> ConflictException")
    void update_notEditableStatus_throws() {
        var event = factory.pendingMadridAgainstYou();
        var req = new SubmitEventRequest(
                MOCK_TITLE,
                "Desc",
                TestDates.rangeStart(),
                TestDates.rangeEnd(),
                MOCK_WIZINK,
                factory.madrid().getId(),
                TestConstants.MADRID,
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.update(event.getId(), req))
                .isInstanceOf(ConflictException.class)
                .hasMessage(ErrorMessages.EVENT_NOT_EDITABLE);
    }

    @Test
    @DisplayName("delete: ok -> elimina el evento")
    void delete_ok_deleteEvent() {
        var event = factory.pendingMadridAgainstYou();

        service.delete(event.getId());

        var reloaded = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.ERASED);
    }

    @Test
    @DisplayName("delete: evento ya eliminado -> ok (no hace nada)")
    void delete_alreadyErased_ok() {
        var event = factory.pendingMadridAgainstYou();

        service.delete(event.getId());

        var reloaded = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.ERASED);
    }

    @Test
    @DisplayName("delete: evento no existe -> NotFoundException")
    void delete_missingEvent_throws() {
        assertThatThrownBy(() -> service.delete(UUID.fromString("cccccccc-0000-0000-0000-000000000099")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorMessages.EVENT_NOT_FOUND);
    }

    @Test
    @DisplayName("delete: evento no pertenece al usuario -> ForbiddenException")
    void delete_notOwner_throws() {
        var event = factory.pendingEvent("Titulo", factory.sevilla(), "Sevilla", "Sala X", TestDates.tomorrow(),
                TestConstants.MOCK_MODERATOR_ID, TestDates.yesterday(), null, null, TestConstants.MOCK_ARTIST_NAME_AY); // Otro usuario lo crea

        assertThatThrownBy(() -> service.delete(event.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ErrorMessages.EVENT_NOT_OWNER);
    }

    @Test
    @DisplayName("delete: evento en estado no eliminable -> ConflictException")
    void delete_notEditableStatus_throws() {
        var event = factory.canceledBarcelonaManifa();

        assertThatThrownBy(() -> service.delete(event.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage(ErrorMessages.EVENT_NOT_ERASABLE);
    }

    @Test
    @DisplayName("delete: evento aprobado no eliminable -> ConflictException")
    void delete_approvedEvent_throws() {
        var event = factory.approvedMadridAgainstYou();

        assertThatThrownBy(() -> service.delete(event.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage(ErrorMessages.EVENT_NOT_ERASABLE_APPROVED);
    }
}
