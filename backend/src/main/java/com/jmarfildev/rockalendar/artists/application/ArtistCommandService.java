package com.jmarfildev.rockalendar.artists.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.artists.api.dto.CreateArtistRequest;
import com.jmarfildev.rockalendar.artists.domain.Artist;
import com.jmarfildev.rockalendar.artists.persistence.ArtistRepository;
import com.jmarfildev.rockalendar.common.SlugNormalizer;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;

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
@Service
@RequiredArgsConstructor
public class ArtistCommandService {

    private final ArtistRepository artistRepository;

    public Artist createArtist(CreateArtistRequest req, UUID userId) {
        String displayName = req.name().trim();
        String slug = SlugNormalizer.of(displayName);

        if (slug.isBlank()) {
            throw new BadRequestException(ErrorMessages.ARTIST_REQUIRED);
        }

        if (artistRepository.existsBySlug(slug)) {
            throw new ConflictException(ErrorMessages.ARTIST_ALREADY_EXISTS);
        }

        return artistRepository.save(Artist.builder()
                .name(displayName)
                .slug(slug)
                .createdByUserId(userId)
                .build());
    }
}
