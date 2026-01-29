package com.jmarfildev.rockalendar.events.application;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.Constants;
import com.jmarfildev.rockalendar.common.SlugNormalizer;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicDto;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapper;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.events.persistence.EventSearchPublicRepository;

/**
 * @author jmarfil
 * <br/><br/>
 * Servicio con métodos para <b>realizar consultas</b> de Eventos:
 * <ul>
 * <li>buscar eventos públicos</li>
 * <li>listar mis eventos</li>
 * <li>obtener detalle</li>
 * </ul>
 *
 */
@Service
@RequiredArgsConstructor
public class EventQueryService {

    private final EventRepository eRepository;
    private final EventSearchPublicRepository espRepository;
    private final EventMapper mapper;

    @Transactional(readOnly = true)
    public Page<EventPublicDto> searchPublic(Optional<String> query,
                                             Optional<OffsetDateTime> dateFrom,
                                             Optional<OffsetDateTime> dateTo,
                                             Optional<UUID> provinceId,
                                             Optional<String> city,
                                             Optional<String> artist,
                                             Pageable pageable) {
        if (dateFrom.isPresent() && dateTo.isPresent() && dateFrom.get().isAfter(dateTo.get())) {
            throw new BadRequestException(ErrorMessages.INVALID_DATE_RANGE);
        }

        if (pageable.getPageSize() > Constants.maxPageSize) {
            throw new BadRequestException(ErrorMessages.PAGE_SIZE_TOO_LARGE);
        }

        String q = query.map(String::trim).orElse("");
        if (q.isBlank()) {
            q = "";
        }

        double minSim = recommendMinSimilarity(q);
        double ftsW = 2.0;
        double trgmW = 1.0;

        var from = dateFrom.orElse(null);
        var to = dateTo.orElse(null);
        var provId = provinceId.orElse(null);

        String citySlug = city.map(SlugNormalizer::of).filter(s -> !s.isBlank()).orElse(null);
        String artistSlug = artist.map(SlugNormalizer::of).filter(s -> !s.isBlank()).orElse(null);

        var results = espRepository.searchPublicEvents(q, minSim, ftsW, trgmW, from, to, provId, citySlug, artistSlug, pageable)
                .map(mapper::toPublicDto);

        // Si no hay resultados y la query tiene más de dos palabras se intenta la segunda consulta
        if (hasMultipleTokens(q) && results.isEmpty()) {
            return espRepository.searchPublicEventsFallback(q, minSim, ftsW, trgmW, from, to, provId, citySlug, artistSlug, pageable)
                    .map(mapper::toPublicDto);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public EventPublicDto getPublicById(UUID id) {
        return eRepository.findByIdAndStatus(id, EventStatus.APPROVED)
                .map(mapper::toPublicDto)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.EVENT_NOT_FOUND));
    }

    private double recommendMinSimilarity(String q) {
        int len = q == null ? 0 : q.length();
        if (len <= 0) {
            return 1.0;
        }
        if (len <= 3) {
            return 0.15;
        }
        if (len <= 6) {
            return 0.25;
        }
        if (len <= 12) {
            return 0.30;
        }
        return 0.35;
    }

    private boolean hasMultipleTokens(String q) {
        if (q == null) {
            return false;
        }
        String trimmed = q.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        return trimmed.split("\\s+").length >= 2;
    }
}
