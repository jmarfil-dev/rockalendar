package com.jmarfildev.rockalendar.artists.application;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.artists.persistence.ArtistRepository;
import com.jmarfildev.rockalendar.common.SlugNormalizer;

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
public class ArtistQueryService {

    private final ArtistRepository artistRepository;

    @Transactional(readOnly = true)
    public List<ArtistDto> searchArtistsAutocomplete(String query) {
        String qRaw = query == null ? "" : query.trim();
        if (qRaw.isBlank()) {
            return List.of();
        }

        String qSlug = SlugNormalizer.of(qRaw);

        Pageable top10 = PageRequest.of(0, 10);

        return artistRepository.findForAutocomplete(qRaw, qSlug, top10)
                .stream()
                .map(a -> new ArtistDto(a.getId(), a.getName(), a.getSlug()))
                .toList();
    }
}
