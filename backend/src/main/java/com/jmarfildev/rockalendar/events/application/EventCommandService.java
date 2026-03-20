package com.jmarfildev.rockalendar.events.application;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.artists.domain.Artist;
import com.jmarfildev.rockalendar.artists.persistence.ArtistRepository;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.ForbiddenException;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.SlugNormalizer;
import com.jmarfildev.rockalendar.common.helper.StringUtils;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.PossibleDuplicateDto;
import com.jmarfildev.rockalendar.events.api.dto.ProposeEventResponse;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapper;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.DuplicateEventProjection;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.geo.domain.Province;
import com.jmarfildev.rockalendar.geo.persistence.ProvinceRepository;
import com.jmarfildev.rockalendar.moderation.application.AutoModerationService;

/**
 *
 * Servicio con los métodos para <b>casos de uso que modifican</b>
 * Eventos:
 *         <ul>
 *         <li>crear/proponer eventos</li>
 *         <li>aprobar/rechazar (cuando entre moderación)</li>
 *         <li>cambiar estado</li>
 *         <li>aplicar reglas de negocio</li>
 *         <li>tocar varias entidades en una transacción</li>
 *         </ul>
 *
 * @author jmarfil
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventCommandService {

    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;
    private final ProvinceRepository provinceRepository;
    private final EventMapper mapper;
    private final CurrentUser currentUser;
    private final AutoModerationService autoModerationService;

    /**
     * Propone un evento que queda en estado PENDING_MODERATION si todos los datos se validan correctamente.
     * Crea los artistas que no existen. Detecta posibles duplicados antes de guardar.
     *
     * @param req request con los datos del evento
     * @return el evento propuesto, junto con info del posible duplicado si se detectó alguno
     */
    @Transactional
    public ProposeEventResponse propose(SubmitEventRequest req) {
        UUID userId = currentUser.userId();
        boolean isAdmin = currentUser.isAdmin();
        EventInputValidate in = validate(req, userId);

        // El admin publica directamente como APPROVED, sin pasar por auto-moderación
        var modResult = isAdmin ? null : autoModerationService.evaluate(in.title(), in.description(), in.artists(), userId);
        EventStatus initialStatus = isAdmin ? EventStatus.APPROVED
                : (modResult.flagged() ? EventStatus.FLAGGED : EventStatus.PENDING_MODERATION);

        // El admin puede crear ediciones anuales, correcciones, etc. sin detección de duplicados
        UUID possibleDuplicateOfId = null;
        PossibleDuplicateDto possibleDuplicate = null;
        if (!isAdmin) {
            // Detección de posibles duplicados antes de guardar (así el nuevo evento no se encuentra a sí mismo)
            var artistIds = in.artists().stream().map(a -> a.getId()).toList();
            var dayStart = in.startDateTime().truncatedTo(ChronoUnit.DAYS);
            var dayEnd = dayStart.plusDays(1);
            List<DuplicateEventProjection> duplicates =
                    eventRepository.findPossibleDuplicate(dayStart, dayEnd, artistIds, in.venueName(), in.title());

            possibleDuplicateOfId = duplicates.isEmpty() ? null : duplicates.get(0).getId();
            possibleDuplicate = duplicates.isEmpty() ? null
                    : new PossibleDuplicateDto(
                            duplicates.get(0).getId(),
                            duplicates.get(0).getTitle(),
                            EventStatus.APPROVED.name().equals(duplicates.get(0).getStatus()));
        }

        var event = Event.builder()
                         .title(in.title())
                         .description(in.description())
                         .startDateTime(in.startDateTime())
                         .endDateTime(in.endDateTime())
                         .province(in.province())
                         .cityName(in.cityName())
                         .citySlug(in.citySlug())
                         .venueName(in.venueName())
                         .venueSlug(in.venueSlug())
                         .sourceUrl(in.sourceUrl())
                         .status(initialStatus)
                         .createdByUserId(userId)
                         .submittedAt(OffsetDateTime.now())
                         .artists(in.artists())
                         .possibleDuplicateOf(possibleDuplicateOfId)
                         .build();

        var saved = eventRepository.save(event);

        if (modResult != null && modResult.flagged()) {
            autoModerationService.logFlag(saved.getId(), modResult);
        }
        if (possibleDuplicateOfId != null) {
            log.info("possible duplicate detected eventId={} duplicateId={}", saved.getId(), possibleDuplicateOfId);
        }

        log.info("event proposed eventId={} userId={} status={} isAdmin={}", saved.getId(), userId, initialStatus, isAdmin);
        return new ProposeEventResponse(mapper.toPrivateDto(saved), possibleDuplicate);
    }

    /**
     * Actualiza un evento que queda en estado PENDING_MODERATION si todos los datos se validan correctamente.
     * Crea los artistas que no existen.
     *
     * @param eventId id del evento a actualizar
     * @param req request con los datos del evento
     * @return el evento actualizado
     */
    @Transactional
    public EventPrivateDto update(UUID eventId, SubmitEventRequest req) {
        UUID userId = currentUser.userId();
        boolean isAdmin = currentUser.isAdmin();
        EventInputValidate in = validate(req, userId);

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));

        // El admin puede editar eventos de cualquier usuario
        if (!isAdmin && !userId.equals(event.getCreatedByUserId())) {
            throw new ForbiddenException(ErrorConstants.EVENT_NOT_OWNER);
        }
        // El admin puede editar en cualquier estado (REJECTED, HIDDEN, etc.)
        if (!isAdmin && !hasEditableStatus(event.getStatus())) {
            throw new ConflictException(ErrorConstants.EVENT_NOT_EDITABLE, ErrorConstants.TYPE_409_EVENT_STATE);
        }

        event.setTitle(in.title());
        event.setDescription(in.description());
        event.setStartDateTime(in.startDateTime());
        event.setEndDateTime(in.endDateTime());
        event.setProvince(in.province());
        event.setCityName(in.cityName());
        event.setCitySlug(in.citySlug());
        event.setVenueName(in.venueName());
        event.setVenueSlug(in.venueSlug());
        event.setSourceUrl(in.sourceUrl());
        event.setModerationMessage(null);

        // Borrar lista anterior y poner nueva
        event.getArtists().clear();
        event.getArtists().addAll(in.artists());

        if (isAdmin) {
            // El admin publica directamente como APPROVED
            event.setStatus(EventStatus.APPROVED);
        } else {
            // Reenviar a moderación (con comprobación de moderación automática)
            var modResult = autoModerationService.evaluate(in.title(), in.description(), in.artists(), userId);
            event.setStatus(modResult.flagged() ? EventStatus.FLAGGED : EventStatus.PENDING_MODERATION);
            if (modResult.flagged()) {
                autoModerationService.logFlag(eventId, modResult);
            }
        }
        event.setSubmittedAt(OffsetDateTime.now());

        // No hace falta save() porque al ser un evento administrado por JPA (viene de un find()) se actualiza al terminar la transacción.
        log.info("event updated eventId={} userId={} status={} isAdmin={}", eventId, userId, event.getStatus(), isAdmin);
        return mapper.toPrivateDto(event);
    }

    @Transactional
    public void delete(UUID eventId) {
        UUID userId = currentUser.userId();
        boolean isAdmin = currentUser.isAdmin();

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));

        // El admin puede eliminar eventos de cualquier usuario
        if (!isAdmin && !userId.equals(event.getCreatedByUserId())) {
            throw new ForbiddenException(ErrorConstants.EVENT_NOT_OWNER);
        }

        if (event.getStatus() == EventStatus.ERASED) {
            return; // idempotente
        }

        // El admin tiene control total del catálogo: puede eliminar en cualquier estado
        if (!isAdmin) {
            // TODO: en frontend mensaje de "contactar con administración"
            if (event.getStatus() == EventStatus.APPROVED) {
                throw new ConflictException(ErrorConstants.EVENT_NOT_ERASABLE_APPROVED, ErrorConstants.TYPE_409_EVENT_STATE);
            }

            if (event.getStatus() != EventStatus.PENDING_MODERATION && event.getStatus() != EventStatus.NEEDS_CHANGES) {
                throw new ConflictException(ErrorConstants.EVENT_NOT_ERASABLE, ErrorConstants.TYPE_409_EVENT_STATE);
            }
        }

        event.setStatus(EventStatus.ERASED);
        log.info("event deleted eventId={} userId={} isAdmin={}", eventId, userId, isAdmin);
    }

    /**
     * Valida los datos comunes para proponer o actualizar un evento.
     * <ul>
     * <li> Rango de fechas correcto </li>
     * <li> Nombre de artistas correctos tras normalizar </li>
     * <li> Título obligatorio </li>
     * <li> Ciudad obligatoria </li>
     * <li> Recinto obligatorio </li>
     * <li> Id de provincia correcto </li>
     * </ul>
     *
     * @param req request con los datos del evento
     * @return un record con los datos validados
     */
    private EventInputValidate validate(SubmitEventRequest req, UUID userId) {
        if (req.endDateTime() != null && req.endDateTime().isBefore(req.startDateTime())) {
            throw new BadRequestException(ErrorConstants.INVALID_DATE_RANGE);
        }

        // Artistas normalizados (descarta blancos tras trim/slug)
        var artists = new LinkedHashSet<Artist>();
        for (String rawName : req.artists()) {
            var displayName = rawName.trim();
            var slug = SlugNormalizer.of(displayName);

            if (slug.isBlank()) {
                continue; // Aunque tiene @NotBlank, aquí puede quedar vacío si es solo símbolos/diacríticos/etc.
            }

            var artist = artistRepository.findBySlug(slug)
                                         .orElseGet(() -> saveArtistOrFetch(displayName, slug, userId));

            artists.add(artist);
        }

        if (artists.isEmpty()) {
            throw new BadRequestException(ErrorConstants.ARTIST_REQUIRED);
        }

        var title = req.title().trim();
        var cityName = req.cityName().trim();
        var venueName = req.venueName().trim();

        var citySlug = SlugNormalizer.of(cityName);
        var venueSlug = SlugNormalizer.of(venueName);

        if (citySlug.isBlank()) {
            throw new BadRequestException(ErrorConstants.CITY_REQUIRED);
        }
        if (venueSlug.isBlank()) {
            throw new BadRequestException(ErrorConstants.VENUE_REQUIRED);
        }
        if (SlugNormalizer.of(title).isBlank()) {
            throw new BadRequestException(ErrorConstants.TITLE_REQUIRED);
        }

        Province province =
                provinceRepository.findById(req.provinceId()).orElseThrow(() -> new BadRequestException(ErrorConstants.INVALID_PROVINCE));

        String description = StringUtils.blankToNull(req.description());
        String sourceUrl = StringUtils.blankToNull(req.sourceUrl());

        // TODO: Evitar eventos duplicados

        return new EventInputValidate(title, description, req.startDateTime(), req.endDateTime(), province, cityName, citySlug, venueName,
                                      venueSlug, sourceUrl, artists);
    }

    /**
     * Condición de carrera: si dos eventos se proponen a la vez con el mismo artista nuevo,
     * uno de los save() falla con DataIntegrityViolationException → reutilizamos el que ganó
     *
     * @param displayName
     * @param slug
     * @return
     */
    private Artist saveArtistOrFetch(String displayName, String slug, UUID userId) {
        try {
            return artistRepository.saveAndFlush(Artist.builder().name(displayName).slug(slug).createdByUserId(userId).build());
        }
        catch (DataIntegrityViolationException ex) {
            log.warn("artist race condition resolved slug={}", slug);
            return artistRepository.findBySlug(slug)
                                   .orElseThrow(() -> new IllegalStateException("Artist slug conflict but not found: " + slug));
        }
    }

    private boolean hasEditableStatus(EventStatus status) {
        return status == EventStatus.DRAFT || status == EventStatus.NEEDS_CHANGES || status == EventStatus.APPROVED;
    }

    private record EventInputValidate(String title,
                                      String description,
                                      OffsetDateTime startDateTime,
                                      OffsetDateTime endDateTime,
                                      Province province,
                                      String cityName,
                                      String citySlug,
                                      String venueName,
                                      String venueSlug,
                                      String sourceUrl,
                                      Set<Artist> artists) {}
}
