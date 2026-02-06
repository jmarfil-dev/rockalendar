package com.jmarfildev.rockalendar.events.application;

import java.util.LinkedHashSet;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.artists.domain.Artist;
import com.jmarfildev.rockalendar.artists.persistence.ArtistRepository;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.SlugNormalizer;
import com.jmarfildev.rockalendar.common.helper.StringUtils;
import com.jmarfildev.rockalendar.events.api.dto.ProposeEventRequest;
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
    private final CurrentUser currentUser;

    @Transactional
    public Event propose(ProposeEventRequest req) {
        UUID userId = currentUser.userId();

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

        // Aunque tiene @NotBlank, aquí pueden quedar vacíos si son solo símbolos/diacríticos/etc.
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

        var event = Event.builder()
                .title(title)
                .description(description)
                .startDateTime(req.startDateTime())
                .endDateTime(req.endDateTime())
                .province(province)
                .cityName(cityName)
                .citySlug(citySlug)
                .venueName(venueName)
                .venueSlug(venueSlug)
                .sourceUrl(sourceUrl)
                .status(EventStatus.PENDING_MODERATION)
                .createdByUserId(userId)
                .artists(artists)
                .build();

        return eventRepository.save(event);
    }
}
