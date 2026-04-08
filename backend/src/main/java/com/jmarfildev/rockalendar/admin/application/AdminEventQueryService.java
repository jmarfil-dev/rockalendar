package com.jmarfildev.rockalendar.admin.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.admin.api.dto.AdminEventListItemDto;
import com.jmarfildev.rockalendar.admin.persistence.AdminEventRepository;
import com.jmarfildev.rockalendar.common.CommonValidations;
import com.jmarfildev.rockalendar.common.helper.SortUtils;
import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEventQueryService {

    private final AdminEventRepository repository;

    private static final List<EventStatus> DEFAULT_STATUSES = List.of(EventStatus.APPROVED);

    private static final Map<String, String> ADMIN_SORT_MAP = Map.of(
            "title", "title",
            "date", "startDateTime",
            "province", "province.name",
            "status", "status");
    private static final Sort ADMIN_DEFAULT_SORT =
            Sort.by(Sort.Order.asc("startDateTime"), Sort.Order.asc("province.name"), Sort.Order.asc("title"));

    public Page<AdminEventListItemDto> listEvents(List<EventStatus> statuses,
                                                  Optional<Short> provinceId,
                                                  Optional<OffsetDateTime> dateFrom,
                                                  Optional<OffsetDateTime> dateTo,
                                                  Optional<String> q,
                                                  Pageable pageable) {
        CommonValidations.validatePageable(pageable);

        if (dateFrom.isPresent() && dateTo.isPresent()) {
            CommonValidations.validateDateRange(dateFrom.get(), dateTo.get());
        }

        List<EventStatus> effectiveStatuses = (statuses == null || statuses.isEmpty()) ? DEFAULT_STATUSES : statuses;

        // Construye el LIKE en el servicio para evitar concatenación en JPQL
        String titleLike = q.map(String::trim)
                            .filter(s -> !s.isBlank())
                            .map(s -> "%" + s.toLowerCase() + "%")
                            .orElse(null);

        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                                                   SortUtils.toJpaSort(pageable, ADMIN_SORT_MAP, ADMIN_DEFAULT_SORT, "id"));

        return repository.findAdminEvents(effectiveStatuses,
                                          provinceId.orElse(null),
                                          dateFrom.orElse(null),
                                          dateTo.orElse(null),
                                          titleLike,
                                          pageableWithSort);
    }
}
