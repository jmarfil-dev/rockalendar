package com.jmarfildev.rockalendar.artists.application;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.artists.api.mapper.ArtistMapper;
import com.jmarfildev.rockalendar.artists.persistence.ArtistRepository;
import com.jmarfildev.rockalendar.common.dto.ComboItemDto;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
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
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistQueryService {

    private final ArtistRepository repository;
    private final ArtistMapper mapper;

    /**
     * Busca artistas. Normaliza query para buscar por nombre y slug.
     *
     * @param query texto libre de búsqueda
     * @return lista de artistas con entre 0 y 10 resultados
     */
    public List<ComboItemDto> searchArtistsAutocomplete(String query) {
        String qRaw = query == null ? "" : query.trim();
        if (qRaw.isBlank()) {
            return List.of();
        }

        String qSlug = SlugNormalizer.of(qRaw);
        Pageable top10 = PageRequest.of(0, 10);

        return repository.findForAutocomplete(qRaw, qSlug, top10);
    }

    public ArtistDto getById(UUID id) {
        return repository.findById(id).map(mapper::toDto).orElseThrow(() -> new NotFoundException(ErrorConstants.ARTIST_NOT_FOUND));
    }
}
