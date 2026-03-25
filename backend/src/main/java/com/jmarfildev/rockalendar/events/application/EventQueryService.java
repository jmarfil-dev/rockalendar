package com.jmarfildev.rockalendar.events.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.ForbiddenException;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.SlugNormalizer;
import com.jmarfildev.rockalendar.common.helper.SortUtils;
import com.jmarfildev.rockalendar.common.helper.SortUtils.SortChoice;
import com.jmarfildev.rockalendar.config.properties.PublicSearchProperties;
import com.jmarfildev.rockalendar.common.helper.StringUtils;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicListItemDto;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapper;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventPublicSearchProjection;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;

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
@Transactional(readOnly = true)
public class EventQueryService {

    private final EventRepository repository;
    private final EventMapper mapper;
    private final PublicSearchProperties props;
    private final CurrentUser currentUser;

    private static final Set<String> SEARCH_SORT_ALLOW = Set.of("relevance", "date", "title", "province", "city");
    private static final Map<String, String> SEARCH_SORT_ALIASES =
            Map.of("startDateTime", "date", "provinceName", "province", "cityName", "city", "start_date_time", "date", "province_name",
                   "province", "city_name", "city");
    private static final Map<String, String> HOME_SORT_ALLOW =
            Map.of("title", "title", "date", "startDateTime", "province", "province.name", "city", "cityName");
    private static final Map<String, String> MINE_SORT_ALLOW =
            Map.of("title", "title", "date", "submittedAt", "province", "province.name", "city", "cityName", "status", "status");
    private static final Sort DEFAULT_SORT_JPA =
            Sort.by(Sort.Order.asc("startDateTime"), Sort.Order.asc("province.name"), Sort.Order.asc("title"));

    public Page<EventPublicListItemDto> listHome(Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                                                   SortUtils.toJpaSort(pageable, HOME_SORT_ALLOW, DEFAULT_SORT_JPA, "id"));
        return repository.findHome(pageableWithSort);
    }

    public Page<EventPublicListItemDto> searchPublic(Optional<String> query,
                                                     Optional<OffsetDateTime> dateFrom,
                                                     Optional<OffsetDateTime> dateTo,
                                                     Optional<UUID> provinceId,
                                                     Optional<String> city,
                                                     Optional<UUID> artistId,
                                                     Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        if (dateFrom.isPresent() && dateTo.isPresent() && dateFrom.get().isAfter(dateTo.get())) {
            throw new BadRequestException(ErrorConstants.INVALID_DATE_RANGE);
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
        var artId = artistId.orElse(null);

        String citySlug = city.map(SlugNormalizer::of).filter(s -> !s.isBlank()).orElse(null);

        String defaultKey = !q.isBlank() ? "relevance" : "date";
        String defaultDir = !q.isBlank() ? "desc" : "asc";
        SortChoice sort = SortUtils.toSqlSortChoice(pageable, SEARCH_SORT_ALLOW, SEARCH_SORT_ALIASES, defaultKey, defaultDir);
        Pageable pageOnly = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<EventPublicSearchProjection> results = repository.searchPublicEvents(q, minSim, ftsW, trgmW, from, to, provId, citySlug, artId,
                                                                                  sort.sortKey(), sort.sortDir(), pageOnly);

        // Si no hay resultados y la query tiene más de dos palabras se intenta la segunda consulta
        if (hasMultipleTokens(q) && results.isEmpty()) {
            return repository.searchPublicEventsFallback(q, minSim, ftsW, trgmW, from, to, provId, citySlug, artId, sort.sortKey(),
                                                         sort.sortDir(), pageOnly)
                             .map(mapper::toPublicListItemDto);
        }

        return results.map(mapper::toPublicListItemDto);
    }

    public EventPublicDto getPublicById(UUID id) {
        return repository.findByIdAndStatus(id, EventStatus.APPROVED)
                         .map(mapper::toPublicDto)
                         .orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));
    }

    public Page<EventPrivateListItemDto> listMine(MeEventTabEnum tab, Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        UUID userId = currentUser.userId();
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                                                   SortUtils.toJpaSort(pageable, MINE_SORT_ALLOW, DEFAULT_SORT_JPA, "id"));
        return switch (tab) {
            case CHANGES -> repository.listMineByStatuses(userId, List.of(EventStatus.NEEDS_CHANGES), pageableWithSort);

            // FLAGGED es un estado interno — el usuario lo ve como pendiente
            case PENDING -> repository.listMineByStatuses(userId, List.of(EventStatus.PENDING_MODERATION, EventStatus.FLAGGED),
                                                          pageableWithSort);

            case OTHERS -> repository.listMineExcludingStatuses(userId, List.of(EventStatus.NEEDS_CHANGES, EventStatus.PENDING_MODERATION,
                                                                                EventStatus.FLAGGED),
                                                                pageableWithSort);

            case ALL -> repository.listMineAllPriorityFutureFirst(userId, pageable);
        };
    }

    public EventPrivateDto getMine(UUID eventId) {
        UUID userId = currentUser.userId();
        Event event = repository.findById(eventId).orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));
        if (!userId.equals(event.getCreatedByUserId())) {
            throw new ForbiddenException(ErrorConstants.EVENT_NOT_OWNER);
        }
        return mapper.toPrivateDto(event);
    }

    private boolean hasMultipleTokens(String q) {
        String trimmed = StringUtils.blankToNull(q);
        return trimmed != null && trimmed.split("\\s+").length >= 2;
    }
}
