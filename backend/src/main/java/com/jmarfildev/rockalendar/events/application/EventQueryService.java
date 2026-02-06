package com.jmarfildev.rockalendar.events.application;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.CommonValidations;
import com.jmarfildev.rockalendar.common.SlugNormalizer;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.config.PublicSearchProperties;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
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
    private final PublicSearchProperties props;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public Page<EventPublicDto> listHome(Pageable pageable) {
        CommonValidations.validatePageable(pageable);

        // "sanitiza" el sort (tu SQL ya tiene ORDER BY fijo)
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        return espRepository.findHome(safePageable).map(mapper::toPublicDto);
    }

    @Transactional(readOnly = true)
    public Page<EventPublicDto> searchPublic(Optional<String> query,
                                             Optional<OffsetDateTime> dateFrom,
                                             Optional<OffsetDateTime> dateTo,
                                             Optional<UUID> provinceId,
                                             Optional<String> city,
                                             Optional<String> artist,
                                             Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        if (dateFrom.isPresent() && dateTo.isPresent() && dateFrom.get().isAfter(dateTo.get())) {
            throw new BadRequestException(ErrorMessages.INVALID_DATE_RANGE);
        }

        String q = query.map(String::trim).orElse("");
        if (q.isBlank()) {
            q = "";
        }

        double minSim = props.minSimilarity();
        double ftsW = props.ftsWeight();
        double trgmW = props.trgmWeight();

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

    @Transactional(readOnly = true)
    public Page<EventPrivateDto> listMine(Pageable pageable) {
        UUID userId = currentUser.userId();
        CommonValidations.validatePageable(pageable);
        return eRepository.listMineOrderFutureFirst(userId, pageable).map(mapper::toPrivateDto);
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
