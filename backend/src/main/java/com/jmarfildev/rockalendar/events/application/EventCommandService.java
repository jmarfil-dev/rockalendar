package com.jmarfildev.rockalendar.events.application;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.artists.domain.Artist;
import com.jmarfildev.rockalendar.artists.persistence.ArtistRepository;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.common.error.ForbiddenException;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.SlugNormalizer;
import com.jmarfildev.rockalendar.common.helper.StringUtils;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapper;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.geo.domain.Province;
import com.jmarfildev.rockalendar.geo.persistence.ProvinceRepository;

/**
 * @author jmarfil
 * <br/><br/>
 * Servicio con los métodos para <b>casos de uso que modifican</b> Eventos:
 * <ul>
 * <li>crear/proponer eventos</li>
 * <li>aprobar/rechazar (cuando entre moderación)</li>
 * <li>cambiar estado</li>
 * <li>aplicar reglas de negocio</li>
 * <li>tocar varias entidades en una transacción</li>
 * </ul>
 *
 */
@Service
@RequiredArgsConstructor
public class EventCommandService {

    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;
    private final ProvinceRepository provinceRepository;
    private final EventMapper mapper;
    private final CurrentUser currentUser;

    @Transactional
    public EventPrivateDto propose(SubmitEventRequest req) {
        UUID userId = currentUser.userId();
        EventInputValidate in = validate(req);

        var event = Event.builder()
                .title(in.title)
                .description(in.description)
                .startDateTime(in.startDateTime())
                .endDateTime(in.endDateTime())
                .province(in.province)
                .cityName(in.cityName)
                .citySlug(in.citySlug)
                .venueName(in.venueName)
                .venueSlug(in.venueSlug)
                .sourceUrl(in.sourceUrl)
                .status(EventStatus.PENDING_MODERATION)
                .createdByUserId(userId)
                .submittedAt(OffsetDateTime.now())
                .artists(in.artists)
                .build();

        return mapper.toPrivateDto(eventRepository.save(event));
    }

    @Transactional
    public EventPrivateDto update(UUID eventId, SubmitEventRequest req) {
        UUID userId = currentUser.userId();
        EventInputValidate in = validate(req);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.EVENT_NOT_FOUND));

        if (!userId.equals(event.getCreatedByUserId())) {
            throw new ForbiddenException(ErrorMessages.EVENT_NOT_OWNER);
        }
        if (!hasEditableStatus(event.getStatus())) {
            throw new ConflictException(ErrorMessages.EVENT_NOT_EDITABLE, ErrorMessages.TYPE_409_EVENT_STATE);
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

        // Borrar lista anterior y poner nueva
        event.getArtists().clear();
        event.getArtists().addAll(in.artists());

        // Reenviar a moderación
        event.setStatus(EventStatus.PENDING_MODERATION);
        event.setSubmittedAt(OffsetDateTime.now());

        // No hace falta save() porque al ser un evento administrado por JPA (vien de un find()) se actualiza al terminar la transacción.
        return mapper.toPrivateDto(event);
    }

    private EventInputValidate validate(SubmitEventRequest req) {
        if (req.endDateTime() != null && req.endDateTime().isBefore(req.startDateTime())) {
            throw new BadRequestException(ErrorMessages.INVALID_EVENT_DATE);
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
                    .orElseGet(() -> artistRepository.save(
                            Artist.builder()
                                    .name(displayName)
                                    .slug(slug)
                                    .build()));

            artists.add(artist);
        }

        if (artists.isEmpty()) {
            throw new BadRequestException(ErrorMessages.ARTIST_REQUIRED);
        }

        var title = req.title().trim();
        var cityName = req.cityName().trim();
        var venueName = req.venueName().trim();

        var citySlug = SlugNormalizer.of(cityName);
        var venueSlug = SlugNormalizer.of(venueName);

        if (citySlug.isBlank()) {
            throw new BadRequestException(ErrorMessages.CITY_REQUIRED);
        }
        if (venueSlug.isBlank()) {
            throw new BadRequestException(ErrorMessages.VENUE_REQUIRED);
        }
        if (SlugNormalizer.of(title).isBlank()) {
            throw new BadRequestException(ErrorMessages.TITLE_REQUIRED);
        }

        Province province = provinceRepository.findById(req.provinceId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.INVALID_PROVINCE));

        String description = StringUtils.blankToNull(req.description());
        String sourceUrl = StringUtils.blankToNull(req.sourceUrl());

        // TODO: Evitar eventos duplicados

        return new EventInputValidate(
                title,
                description,
                req.startDateTime(),
                req.endDateTime(),
                province,
                cityName,
                citySlug,
                venueName,
                venueSlug,
                sourceUrl,
                artists
        );
    }

    private boolean hasEditableStatus(EventStatus status) {
        return status == EventStatus.DRAFT
                || status == EventStatus.NEEDS_CHANGES
                || status == EventStatus.APPROVED;
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
