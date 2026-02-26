package com.jmarfildev.rockalendar.events.application;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.CommonValidations;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.SlugNormalizer;
import com.jmarfildev.rockalendar.common.helper.StringUtils;
import com.jmarfildev.rockalendar.config.PublicSearchProperties;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicListItemDto;
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

    private static final Map<String, String> SORT_MAP = Map.of(
            "title", "title",
            "date", "startDateTime",
            "province", "province.name",
            "city", "cityName");

    @Transactional(readOnly = true)
    public Page<EventPublicListItemDto> listHome(Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortCriteria(pageable));
        return eRepository.findHome(pageableWithSort);
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
    public Page<EventPrivateListItemDto> listMine(MeEventTabEnum tab, Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        UUID userId = currentUser.userId();
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortCriteria(pageable));
        return switch (tab) {
            case CHANGES -> eRepository.listMineByStatus(userId, EventStatus.NEEDS_CHANGES, pageableWithSort);

            case PENDING -> eRepository.listMineByStatus(userId, EventStatus.PENDING_MODERATION, pageableWithSort);

            case OTHERS -> eRepository.listMineExcludingStatuses(userId,
                                                                 List.of(EventStatus.NEEDS_CHANGES, EventStatus.PENDING_MODERATION),
                                                                 pageableWithSort);

            case ALL -> eRepository.listMineAllPriorityFutureFirst(userId, pageable);
        };
    }

    private Sort sortCriteria(Pageable pageable) {
        Sort sort = mapAllowedSort(pageable.getSort());
        if (sort == null) {
            // Ordenación por defecto si sort viene vacío o con valores no válidos
            sort = Sort.by(Sort.Order.asc("startDateTime"), Sort.Order.asc("province.name"), Sort.Order.asc("title"));
        }

        // Estabiliza paginación: siempre añadir id al final
        if (sort.getOrderFor("id") == null) {
            sort = sort.and(Sort.by(Sort.Order.asc("id")));
        }

        return sort;
    }

    private Sort mapAllowedSort(Sort inSort) {
        if (inSort.isUnsorted()) {
            return null;
        }

        List<Sort.Order> orders = new ArrayList<>();

        for (Sort.Order o : inSort) {
            String mapped = SORT_MAP.get(o.getProperty());

            if (mapped == null) {
                // Si el orden no existe en SORT_MAP, lo ignora
                continue;
            }
            orders.add(new Sort.Order(o.getDirection(), mapped));
        }

        if (orders.isEmpty()) {
            return null;
        }

        return Sort.by(orders);
    }

    private boolean hasMultipleTokens(String q) {
        String trimmed = StringUtils.blankToNull(q);
        return trimmed == null ? false : trimmed.split("\\s+").length >= 2;
    }
}
