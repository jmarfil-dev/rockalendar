package com.jmarfildev.rockalendar.support;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
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

    public Artist againstYou() {
        return artist(TestConstants.MOCK_ARTIST_NAME_AY);
    }

    public Artist laPolla() {
        return artist("La Polla Recordas");
    }

    /*
     * Events
     */

    public Event approvedEvent(String title,
                               Province province,
                               String city,
                               String venue,
                               OffsetDateTime start,
                               String... artistNames) {
        return saveEvent(title, province, city, venue, start, EventStatus.APPROVED, TestDates.yesterday(), null, artistNames);
    }

    public Event pendingEvent(String title,
                              Province province,
                              String city,
                              String venue,
                              OffsetDateTime start,
                              OffsetDateTime submittedAt,
                              OffsetDateTime moderatedAtAt,
                              String... artistNames) {
        return saveEvent(title, province, city, venue, start, EventStatus.PENDING_MODERATION, submittedAt, moderatedAtAt, artistNames);
    }

    public Event rejectedEvent(String title,
                               Province province,
                               String city,
                               String venue,
                               OffsetDateTime start,
                               OffsetDateTime submittedAt,
                               OffsetDateTime moderatedAtAt,
                               String... artistNames) {
        return saveEvent(title, province, city, venue, start, EventStatus.REJECTED, submittedAt, moderatedAtAt, artistNames);
    }

    public Event hiddenEvent(String title,
                             Province province,
                             String city,
                             String venue,
                             OffsetDateTime start,
                             OffsetDateTime submittedAt,
                             OffsetDateTime moderatedAtAt,
                             String... artistNames) {
        return saveEvent(title, province, city, venue, start, EventStatus.HIDDEN, submittedAt, moderatedAtAt, artistNames);
    }

    public Event canceledEvent(String title,
                               Province province,
                               String city,
                               String venue,
                               OffsetDateTime start,
                               OffsetDateTime submittedAt,
                               OffsetDateTime moderatedAtAt,
                               String... artistNames) {
        return saveEvent(title, province, city, venue, start, EventStatus.CANCELED, submittedAt, moderatedAtAt, artistNames);
    }

    private Event saveEvent(String title,
                            Province province,
                            String city,
                            String venue,
                            OffsetDateTime start,
                            EventStatus status,
                            OffsetDateTime submittedAt,
                            OffsetDateTime moderatedAtAt,
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
                .createdByUserId(UUID.fromString(TestConstants.MOCK_USER_ID))
                .submittedAt(submittedAt)
                .moderatedByUserId(UUID.fromString(TestConstants.MOCK_MODERATOR_ID))
                .moderatedAt(TestDates.tomorrow())
                .build();

        return eventRepository.save(event);
    }

    public Event approvedMadridAgainstYou() {
        return approvedEvent(
                "%s en %s".formatted(TestConstants.MOCK_ARTIST_NAME_AY, TestConstants.MADRID),
                madrid(),
                TestConstants.MADRID,
                "Sala Copérnico",
                TestDates.madrid(),
                TestConstants.MOCK_ARTIST_NAME_AY);
    }

    public Event approvedBarcelonaBoikot() {
        return approvedEvent(
                "Boikot en %s".formatted(TestConstants.BARCELONA),
                barcelona(),
                TestConstants.BARCELONA,
                "Palau Sant Jordi",
                TestDates.barcelona(),
                "Boikot");
    }

    public Event approvedValenciaPast() {
        return approvedEvent(
                "Desera en València",
                valencia(),
                "València",
                "Sala Moon",
                TestDates.past(),
                "Desera");
    }

    public Event pendingMadridAgainstYou() {
        return pendingEvent(
                "%s en MADRID (pendiente)".formatted(TestConstants.MOCK_ARTIST_NAME_AY),
                madrid(),
                TestConstants.MADRID,
                "Sala Copérnico",
                TestDates.madrid().plusDays(1),
                TestDates.yesterday(),
                null,
                TestConstants.MOCK_ARTIST_NAME_AY);
    }

    public Event pendingValenciaLosDeMarras() {
        return pendingEvent(
                "Los de Marras en València",
                valencia(),
                "València",
                "Sala Moon",
                TestDates.genericFuture(),
                TestDates.yesterday().minusDays(1),
                TestDates.now(),
                "Los de Marras");
    }

    public Event rejectedValenciaMafalda() {
        return rejectedEvent(
                "Mafalda en València",
                valencia(),
                "València",
                "Sala Moon",
                TestDates.past(),
                TestDates.past().minusMonths(2),
                TestDates.past().minusMonths(1),
                "Mafalda");
    }

    public Event hiddenMadridSoziedadAlkoholika() {
        return hiddenEvent(
                "Soziedad Alkoholika en %s".formatted(TestConstants.MADRID),
                madrid(),
                TestConstants.MADRID,
                "Sala Copérnico",
                TestDates.madrid(),
                TestDates.yesterday().minusDays(5),
                TestDates.yesterday().minusDays(4),
                "Soziedad Alkoholika");
    }

    public Event canceledBarcelonaManifa() {
        return canceledEvent(
                "Manifa en %s".formatted(TestConstants.BARCELONA),
                barcelona(),
                TestConstants.BARCELONA,
                "Palau Sant Jordi",
                TestDates.barcelona(),
                TestDates.yesterday().minusDays(2),
                TestDates.now(),
                "Manifa");
    }
}
