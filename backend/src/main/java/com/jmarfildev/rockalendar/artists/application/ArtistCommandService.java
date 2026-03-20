package com.jmarfildev.rockalendar.artists.application;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.artists.api.dto.CreateArtistRequest;
import com.jmarfildev.rockalendar.artists.domain.Artist;
import com.jmarfildev.rockalendar.artists.persistence.ArtistRepository;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.SlugNormalizer;

/**
 * Servicio con los métodos para <b>casos de uso que modifican</b> Artistas:
 * <ul>
 * <li>crear artista</li>
 * <li>modificar artista</li>
 * <li>aplicar reglas de negocio</li>
 * </ul>
 *
 * @author jmarfil
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistCommandService {

    private final ArtistRepository artistRepository;
    private final CurrentUser currentUser;

    @Transactional
    public Artist createArtist(CreateArtistRequest req) {
        UUID userId = currentUser.userId();
        String displayName = req.name().trim();
        String slug = SlugNormalizer.of(displayName);

        if (slug.isBlank()) {
            throw new BadRequestException(ErrorConstants.ARTIST_REQUIRED);
        }

        if (artistRepository.existsBySlug(slug)) {
            throw new ConflictException(ErrorConstants.ARTIST_ALREADY_EXISTS);
        }

        try {
            var artist = artistRepository.saveAndFlush(Artist.builder()
                    .name(displayName)
                    .slug(slug)
                    .createdByUserId(userId)
                    .build());
            log.info("artist created artistId={} slug={}", artist.getId(), slug);
            return artist;
        } catch (DataIntegrityViolationException ex) {
            // Se lanza si otro hilo crea el mismo slug entre el existsBySlug y el save
            throw new ConflictException(ErrorConstants.ARTIST_ALREADY_EXISTS);
        }
    }

    @Transactional
    public void deleteArtist(UUID id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorConstants.ARTIST_NOT_FOUND));
        if (artistRepository.hasEvents(id)) {
            throw new ConflictException(ErrorConstants.ARTIST_HAS_EVENTS);
        }
        artistRepository.delete(artist);
        log.info("artist deleted artistId={} slug={}", artist.getId(), artist.getSlug());
    }
}
