package com.jmarfildev.rockalendar.moderation.application;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.CommonValidations;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.SortUtils;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapper;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApprovedListItemDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedListItemDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingListItemDto;

/**
 * @author jmarfil
 *
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModerationQueryService {

    private final EventRepository repository;
    private final EventMapper eventMapper;

    private static final String FIELD_SUBMITTED_AT = "submittedAt";
    private static final String FIELD_MODERATED_AT = "moderatedAt";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_CREATED = "created";

    private static final Map<String, String> PENDING_SORT_MAP =
            Map.of("submitted", FIELD_SUBMITTED_AT, FIELD_CREATED, FIELD_CREATED_AT, FIELD_TITLE, FIELD_TITLE);
    private static final Sort PENDING_DEFAULT_SORT = Sort.by(Sort.Order.asc(FIELD_SUBMITTED_AT), Sort.Order.asc(FIELD_CREATED_AT));
    private static final Map<String, String> APPROVED_SORT_MAP =
            Map.of("approved", FIELD_MODERATED_AT, FIELD_CREATED, FIELD_CREATED_AT, FIELD_TITLE, FIELD_TITLE);
    private static final Sort APPROVED_DEFAULT_SORT = Sort.by(Sort.Order.desc(FIELD_MODERATED_AT), Sort.Order.asc(FIELD_CREATED_AT));
    private static final Map<String, String> ARCHIVED_SORT_MAP =
            Map.of("moderated", FIELD_MODERATED_AT, FIELD_CREATED, FIELD_CREATED_AT, FIELD_TITLE, FIELD_TITLE, FIELD_STATUS, FIELD_STATUS);
    private static final Sort ARCHIVED_DEFAULT_SORT = Sort.by(Sort.Order.asc(FIELD_MODERATED_AT), Sort.Order.asc(FIELD_CREATED_AT));

    public EventPrivateDto getForModeration(UUID eventId) {
        return repository.findById(eventId)
                         .map(eventMapper::toPrivateDto)
                         .orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));
    }

    public Page<ModerationPendingListItemDto> listPending(Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                                                   SortUtils.toJpaSort(pageable, PENDING_SORT_MAP, PENDING_DEFAULT_SORT, "id"));
        return repository.findPending(pageableWithSort);
    }

    public Page<ModerationApprovedListItemDto> listApproved(Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                                                   SortUtils.toJpaSort(pageable, APPROVED_SORT_MAP, APPROVED_DEFAULT_SORT, "id"));
        return repository.findApproved(pageableWithSort);
    }

    public Page<ModerationArchivedListItemDto> listArchived(Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                                                   SortUtils.toJpaSort(pageable, ARCHIVED_SORT_MAP, ARCHIVED_DEFAULT_SORT, "id"));
        return repository.findArchived(pageableWithSort);
    }
}
