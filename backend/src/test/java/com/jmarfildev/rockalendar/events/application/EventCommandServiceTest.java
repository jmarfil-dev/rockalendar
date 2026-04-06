package com.jmarfildev.rockalendar.events.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.jmarfildev.rockalendar.artists.domain.Artist;
import com.jmarfildev.rockalendar.artists.persistence.ArtistRepository;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.SlugNormalizer;
import com.jmarfildev.rockalendar.common.storage.ImageProcessingService;
import com.jmarfildev.rockalendar.common.storage.StorageService;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapperImpl;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.moderation.application.AutoModerationService;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;
import com.jmarfildev.rockalendar.support.TestDates;

/**
 * @author jmarfil
 *
 */
@DataJpaTest
@Import({ EventCommandService.class, DatabaseCleaner.class, TestDataFactory.class, EventMapperImpl.class,
        AutoModerationService.class })
class EventCommandServiceTest extends AbstractPostgresTest {

    @Autowired
    EventCommandService service;
    @Autowired
    DatabaseCleaner cleaner;
    @Autowired
    TestDataFactory factory;
    @Autowired
    EventRepository eventRepository;
    @MockitoSpyBean
    ArtistRepository artistRepository;

    @MockitoBean
    CurrentUser currentUser;
    @MockitoBean
    StorageService storageService;
    @MockitoBean
    ImageProcessingService imageProcessingService;

    private final String mockTitle = "Concierto";
    private final String mockWizink = "WiZink Center";

    @BeforeEach
    void setUp() {
        cleaner.truncateMutableTables();
        when(currentUser.userId()).thenReturn(UUID.fromString(TestConstants.MOCK_USER_ID));
        when(currentUser.isAdmin()).thenReturn(false);
    }

    private void asAdmin() {
        when(currentUser.userId()).thenReturn(UUID.fromString(TestConstants.MOCK_ADMIN_ID));
        when(currentUser.isAdmin()).thenReturn(true);
    }

    @Test
    @DisplayName("propose: invalid date range -> BadRequestException")
    void propose_invalidDateRange_throws() {
        var req = new SubmitEventRequest(
                mockTitle,
                "Desc",
                TestDates.rangeEndDate(),
                null,
                TestDates.rangeStartDate(),
                mockWizink,
                factory.madrid().getIneCode(),
                TestConstants.MADRID,
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.propose(req, null))
                .isInstanceOf(BadRequestException.class)
                                                      .hasMessage(ErrorConstants.INVALID_DATE_RANGE);
    }

    @Test
    @DisplayName("propose: artists vacíos tras normalizar -> BadRequestException")
    void propose_artistsBecomeEmptyAfterNormalize_throws() {
        var req = new SubmitEventRequest(
                mockTitle,
                null,
                TestDates.madridDate(),
                null,
                null,
                mockWizink,
                factory.madrid().getIneCode(),
                TestConstants.MADRID,
                List.of("   ", "!!!", "´´´"),
                null);

        assertThatThrownBy(() -> service.propose(req, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorConstants.ARTIST_REQUIRED);
    }

    @Test
    @DisplayName("propose: citySlug se queda vacío tras normalizar -> BadRequestException")
    void propose_cityBecomesBlankAfterNormalize_throws() {
        var req = new SubmitEventRequest(
                mockTitle,
                null,
                TestDates.madridDate(),
                null,
                null,
                mockWizink,
                factory.madrid().getIneCode(),
                "!!!",
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.propose(req, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorConstants.CITY_REQUIRED);
    }

    @Test
    @DisplayName("propose: venueSlug se queda vacío tras normalizar -> BadRequestException")
    void propose_venueBecomesBlankAfterNormalize_throws() {
        var req = new SubmitEventRequest(
                mockTitle,
                null,
                TestDates.madridDate(),
                null,
                null,
                "***",
                factory.madrid().getIneCode(),
                TestConstants.MADRID,
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.propose(req, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorConstants.VENUE_REQUIRED);
    }

    @Test
    @DisplayName("propose: title se queda vacío tras normalizar -> BadRequestException")
    void propose_titleBecomesBlankAfterNormalize_throws() {
        var req = new SubmitEventRequest(
                "!!!",
                null,
                TestDates.madridDate(),
                null,
                null,
                mockWizink,
                factory.madrid().getIneCode(),
                TestConstants.MADRID,
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.propose(req, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorConstants.TITLE_REQUIRED);
    }

    @Test
    @DisplayName("propose: provinceId inválida -> BadRequestException")
    void propose_invalidProvince_throws() {
        short missingProv = 99; // ine_code inexistente (rango válido: 1-52)

        var req = new SubmitEventRequest(
                mockTitle,
                null,
                TestDates.madridDate(),
                null,
                null,
                mockWizink,
                missingProv,
                TestConstants.MADRID,
                List.of("Ska-P"),
                null);

        assertThatThrownBy(() -> service.propose(req, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorConstants.INVALID_PROVINCE);
    }

    @Test
    @DisplayName("propose: ok -> crea evento PENDING_MODERATION")
    void propose_ok_createPendingEvent() {
        var req = new SubmitEventRequest(
                "  Ska-P en Madrid  ",
                "  descripcion  ",
                TestDates.madridDate(),
                null,
                null,
                "  WiZink Center  ",
                factory.madrid().getIneCode(),
                "  Madrid  ",
                List.of("  Ska-P  "),
                "  https://example.com  ");

        var saved = service.propose(req, null);

        var reloaded = eventRepository.findById(saved.event().id()).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.PENDING_MODERATION);
        assertThat(reloaded.getCreatedByUserId()).isEqualTo(UUID.fromString(TestConstants.MOCK_USER_ID));

        assertThat(reloaded.getTitle()).isEqualTo("Ska-P en Madrid");
        assertThat(reloaded.getCityName()).isEqualTo(TestConstants.MADRID);
        assertThat(reloaded.getVenueName()).isEqualTo(mockWizink);

        assertThat(reloaded.getCitySlug()).isEqualTo(SlugNormalizer.of(TestConstants.MADRID));
        assertThat(reloaded.getVenueSlug()).isEqualTo(SlugNormalizer.of(mockWizink));

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
                TestDates.madridDate(),
                null,
                null,
                mockWizink,
                factory.madrid().getIneCode(),
                TestConstants.MADRID,
                List.of("  AgAinST- yOU "),
                null);

        var saved = service.propose(req, null);

        var reloaded = eventRepository.findById(saved.event().id()).orElseThrow();

        assertThat(artistRepository.count()).isEqualTo(before); // No crece count porque no vuelve a crear mismo artista
        assertThat(reloaded.getArtists()).singleElement().satisfies(a -> assertThat(a.getId()).isEqualTo(existing.getId()));
    }

    @Test
    @DisplayName("propose: artistas  no se duplican por slug y preservan orden (LinkedHashSet)")
    void propose_deduplicatesArtists_preservesOrder() {
        var req = new SubmitEventRequest(
                mockTitle,
                null,
                TestDates.madridDate(),
                null,
                null,
                mockWizink,
                factory.madrid().getIneCode(),
                TestConstants.MADRID,
                List.of("Ska-P", "  ska p  ", "Boikot"),
                null);

        var saved = service.propose(req, null);

        var reloaded = eventRepository.findById(saved.event().id()).orElseThrow();
        assertThat(reloaded.getArtists())
                .extracting(Artist::getSlug)
                .containsExactly("ska p", "boikot");

        assertThat(artistRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("propose: description y sourceUrl en blanco se guardan como null")
    void propose_blankOptionalFields_becomeNull() {
        var req = new SubmitEventRequest(
                mockTitle,
                "    ",
                TestDates.madridDate(),
                null,
                null,
                mockWizink,
                factory.madrid().getIneCode(),
                TestConstants.MADRID,
                List.of("Ska-P"),
                "   ");

        var saved = service.propose(req, null);

        var reloaded = eventRepository.findById(saved.event().id()).orElseThrow();
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
                TestDates.madridDate(),
                null,
                null,
                "  WiZink Center  ",
                factory.madrid().getIneCode(),
                "  Madrid  ",
                List.of("  Against You  "),
                "  https://example.com  ");

        var updated = service.update(event.getId(), req, null, false);

        var reloaded = eventRepository.findById(updated.id()).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.PENDING_MODERATION);
        assertThat(reloaded.getTitle()).isEqualTo("Against You en concierto");
        assertThat(reloaded.getVenueName()).isEqualTo(mockWizink);
        assertThat(reloaded.getVenueSlug()).isEqualTo(SlugNormalizer.of(mockWizink));
        assertThat(reloaded.getArtists()).hasSize(1);
        assertThat(reloaded.getArtists().iterator().next().getSlug()).isEqualTo(SlugNormalizer.of(TestConstants.MOCK_ARTIST_NAME_AY));
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
    @DisplayName("delete: evento ya eliminado -> ok (idempotente, no lanza excepción)")
    void delete_alreadyErased_ok() {
        var event = factory.pendingMadridAgainstYou();

        service.delete(event.getId()); // primera vez: PENDING_MODERATION → ERASED
        service.delete(event.getId()); // segunda vez: ya ERASED, no lanza excepción

        var reloaded = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.ERASED);
    }

    /*
     * ROLE_ADMIN
     */

    @Test
    @DisplayName("propose (admin): crea evento APPROVED directamente, sin moderación automática")
    void propose_admin_createsApprovedEventDirectly() {
        asAdmin();
        var req = new SubmitEventRequest(
                mockTitle, null, TestDates.madridDate(), null, null, mockWizink,
                factory.madrid().getIneCode(), TestConstants.MADRID,
                List.of("Ska-P"), null);

        var saved = service.propose(req, null);

        var reloaded = eventRepository.findById(saved.event().id()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.APPROVED);
        assertThat(reloaded.getCreatedByUserId()).isEqualTo(UUID.fromString(TestConstants.MOCK_ADMIN_ID));
    }

    @Test
    @DisplayName("propose (admin): no detecta duplicados aunque exista evento similar")
    void propose_admin_skipsDuplicateDetection() {
        // Evento existente que sería detectado como duplicado para un usuario normal
        factory.approvedMadridAgainstYou();
        asAdmin();

        var req = new SubmitEventRequest(
                "%s en %s".formatted(TestConstants.MOCK_ARTIST_NAME_AY, TestConstants.MADRID),
                null, TestDates.madridDate(), null, null, "Sala Copérnico",
                factory.madrid().getIneCode(), TestConstants.MADRID,
                List.of(TestConstants.MOCK_ARTIST_NAME_AY), null);

        var result = service.propose(req, null);

        assertThat(result.possibleDuplicate()).isNull();
        assertThat(result.event().status()).isEqualTo(EventStatus.APPROVED);
    }

    @Test
    @DisplayName("update (admin): puede editar evento de otro usuario")
    void update_admin_canUpdateAnotherUsersEvent() {
        // Evento creado por MOCK_USER_ID; el admin (distinto userId) lo edita
        var event = factory.approvedMadridAgainstYou();
        asAdmin();

        var req = new SubmitEventRequest(
                "Título editado por admin", null, TestDates.madridDate(), null, null, mockWizink,
                factory.madrid().getIneCode(), TestConstants.MADRID,
                List.of(TestConstants.MOCK_ARTIST_NAME_AY), null);

        var updated = service.update(event.getId(), req, null, false);

        assertThat(updated.title()).isEqualTo("Título editado por admin");
        assertThat(updated.status()).isEqualTo(EventStatus.APPROVED);
    }

    @Test
    @DisplayName("update (admin): puede editar evento en estado REJECTED")
    void update_admin_canUpdateRejectedEvent() {
        var event = factory.rejectedValenciaMafalda();
        asAdmin();

        var req = new SubmitEventRequest(
                "Mafalda corregido", null, TestDates.genericFutureDate(), null, null, "Sala Moon",
                factory.valencia().getId(), "València",
                List.of("Mafalda"), null);

        var updated = service.update(event.getId(), req, null, false);

        assertThat(updated.status()).isEqualTo(EventStatus.APPROVED);
    }

    @Test
    @DisplayName("update (admin): puede editar evento en estado HIDDEN")
    void update_admin_canUpdateHiddenEvent() {
        var event = factory.hiddenMadridSoziedadAlkoholika();
        asAdmin();

        var req = new SubmitEventRequest(
                "Soziedad Alkoholika corregido", null, TestDates.madridDate(), null, null, "Sala Copérnico",
                factory.madrid().getIneCode(), TestConstants.MADRID,
                List.of("Soziedad Alkoholika"), null);

        var updated = service.update(event.getId(), req, null, false);

        assertThat(updated.status()).isEqualTo(EventStatus.APPROVED);
    }

    @Test
    @DisplayName("delete (admin): puede eliminar evento de otro usuario")
    void delete_admin_canDeleteAnotherUsersEvent() {
        var event = factory.pendingMadridAgainstYou(); // creado por MOCK_USER_ID
        asAdmin();

        service.delete(event.getId());

        var reloaded = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.ERASED);
    }

    @Test
    @DisplayName("delete (admin): puede eliminar evento APPROVED")
    void delete_admin_canDeleteApprovedEvent() {
        var event = factory.approvedMadridAgainstYou();
        asAdmin();

        service.delete(event.getId());

        var reloaded = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.ERASED);
    }

    @Test
    @DisplayName("delete (admin): puede eliminar evento HIDDEN")
    void delete_admin_canDeleteHiddenEvent() {
        var event = factory.hiddenMadridSoziedadAlkoholika();
        asAdmin();

        service.delete(event.getId());

        var reloaded = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.ERASED);
    }

    @Test
    @DisplayName("delete (admin): puede eliminar evento REJECTED")
    void delete_admin_canDeleteRejectedEvent() {
        var event = factory.rejectedValenciaMafalda();
        asAdmin();

        service.delete(event.getId());

        var reloaded = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.ERASED);
    }

    @Test
    @DisplayName("propose: condición de carrera en saveArtistOrFetch -> reutiliza el artista que ganó la carrera")
    void propose_saveArtistOrFetch_concurrentSave_reusesExistingArtist() {
        var existingArtist = factory.againstYou();
        String slug = SlugNormalizer.of(TestConstants.MOCK_ARTIST_NAME_AY);

        // Simula la condición de carrera: findBySlug vacío → saveAndFlush falla → findBySlug devuelve el ganador
        doReturn(Optional.empty())
                .doReturn(Optional.of(existingArtist))
                .when(artistRepository).findBySlug(slug);
        doThrow(new DataIntegrityViolationException("duplicate key"))
                .when(artistRepository).saveAndFlush(any());

        var req = new SubmitEventRequest(
                mockTitle, null, TestDates.madridDate(), null, null, mockWizink,
                factory.madrid().getIneCode(), TestConstants.MADRID,
                List.of(TestConstants.MOCK_ARTIST_NAME_AY), null);

        var saved = service.propose(req, null);

        var reloaded = eventRepository.findById(saved.event().id()).orElseThrow();
        assertThat(reloaded.getArtists()).singleElement()
                .satisfies(a -> assertThat(a.getId()).isEqualTo(existingArtist.getId()));
    }
}
