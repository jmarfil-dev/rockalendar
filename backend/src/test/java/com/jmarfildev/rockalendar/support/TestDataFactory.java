package com.jmarfildev.rockalendar.support;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.jmarfildev.rockalendar.artists.domain.Artist;
import com.jmarfildev.rockalendar.artists.persistence.ArtistRepository;
import com.jmarfildev.rockalendar.common.SlugNormalizer;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.geo.domain.Province;
import com.jmarfildev.rockalendar.geo.persistence.ProvinceRepository;

/**
 * @author jmarfil
 *
 */
@Component
@Profile("test")
public class TestDataFactory {

    private final ProvinceRepository provinceRepository;
    private final ArtistRepository artistRepository;
    private final EventRepository eventRepository;

    public TestDataFactory(ProvinceRepository provinceRepository,
                           ArtistRepository artistRepository,
                           EventRepository eventRepository) {
        this.provinceRepository = provinceRepository;
        this.artistRepository = artistRepository;
        this.eventRepository = eventRepository;
    }

    /*
     * Provinces
     */

    public Province madrid() {
        return province(TestConstants.INE_MADRID);
    }

    public Province barcelona() {
        return province(TestConstants.INE_BARCELONA);
    }

    public Province valencia() {
        return province(TestConstants.INE_VALENCIA);
    }

    public Province sevilla() {
        return province(TestConstants.INE_SEVILLA);
    }

    public Province province(short ineCode) {
        return provinceRepository.findByIneCode(ineCode)
                .orElseThrow(() -> new IllegalStateException("No existe province con ine_code=" + ineCode));
    }

    /*
     * Artists
     */

    public Artist artist(String name) {
        String slug = SlugNormalizer.of(name);

        return artistRepository.findBySlug(slug)
                .orElseGet(() -> artistRepository.save(Artist.builder()
                        .name(name)
                        .slug(slug)
                        .build()));
    }

    public Set<Artist> artists(String... names) {
        return Arrays.stream(names)
                .map(this::artist)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /*
     * Events
     */

    public Event approvedEvent(String title,
                               short provinceIne,
                               String city,
                               String venue,
                               OffsetDateTime start,
                               String... artistNames) {
        return saveEvent(title, province(provinceIne), city, venue, start, EventStatus.APPROVED, artistNames);
    }

    public Event approvedEvent(String title,
                               Province province,
                               String city,
                               String venue,
                               OffsetDateTime start,
                               String... artistNames) {
        return saveEvent(title, province, city, venue, start, EventStatus.APPROVED, artistNames);
    }

    public Event pendingEvent(String title,
                              short provinceIne,
                              String city,
                              String venue,
                              OffsetDateTime start,
                              String... artistNames) {
        return saveEvent(title, province(provinceIne), city, venue, start, EventStatus.PENDING_MODERATION, artistNames);
    }

    public Event pendingEvent(String title,
                              Province province,
                              String city,
                              String venue,
                              OffsetDateTime start,
                              String... artistNames) {
        return saveEvent(title, province, city, venue, start, EventStatus.PENDING_MODERATION, artistNames);
    }

    private Event saveEvent(String title,
                            Province province,
                            String city,
                            String venue,
                            OffsetDateTime start,
                            EventStatus status,
                            String... artistNames) {
        Event event = Event.builder()
                .title(title)
                .startDateTime(start)
                .province(province)
                .cityName(city)
                .citySlug(SlugNormalizer.of(city))
                .venueName(venue)
                .venueSlug(SlugNormalizer.of(venue))
                .status(status)
                .artists(artists(artistNames))
                .build();

        return eventRepository.save(event);
    }

    public Event approvedMadridAgainstYou() {
        return approvedEvent(
                "Against You en Madrid",
                madrid(),
                "Madrid",
                "Sala Copérnico",
                OffsetDateTime.parse(TestConstants.MADRID_DATE),
                "Against You");
    }

    public Event pendingMadridAgainstYou() {
        return pendingEvent(
                "Against You en MADRID (pendiente)",
                madrid(),
                "Madrid",
                "Sala Copérnico",
                OffsetDateTime.parse("2026-03-16T21:00:00Z"),
                "Against You");
    }

    public Event approvedBarcelonaBoikot() {
        return approvedEvent(
                "Boikot en Barcelona",
                barcelona(),
                "Barcelona",
                "Palau Sant Jordi",
                OffsetDateTime.parse(TestConstants.BARCELONA_DATE),
                "Boikot");
    }

    public Event pendingValenciaLosDeMarras() {
        return pendingEvent(
                "Los de Marras en València",
                valencia(),
                "València",
                "Sala Moon",
                OffsetDateTime.parse(TestConstants.GENERIC_DATE),
                "Los de Marras");
    }
}
