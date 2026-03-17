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
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedListItemDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingListItemDto;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationEventRepository;

/**
 * @author jmarfil
 *
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModerationQueryService {

    private final ModerationEventRepository repository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private static final Map<String, String> PENDING_SORT_MAP = Map.of("submitted", "submittedAt", "created", "createdAt", "title", "title");
    private static final Sort PENDING_DEFAULT_SORT = Sort.by(Sort.Order.asc("submittedAt"), Sort.Order.asc("createdAt"));
    private static final Map<String, String> ARCHIVED_SORT_MAP =
            Map.of("moderated", "moderatedAt", "created", "createdAt", "title", "title", "status", "status");
    private static final Sort ARCHIVED_DEFAULT_SORT = Sort.by(Sort.Order.asc("moderatedAt"), Sort.Order.asc("createdAt"));

    public EventPrivateDto getForModeration(UUID eventId) {
        return eventRepository.findById(eventId)
                              .map(eventMapper::toPrivateDto)
                              .orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));
    }

    public Page<ModerationPendingListItemDto> listPending(Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                                                   SortUtils.toJpaSort(pageable, PENDING_SORT_MAP, PENDING_DEFAULT_SORT, "id"));
        return repository.findPending(pageableWithSort);
    }

    public Page<ModerationArchivedListItemDto> listArchived(Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                                                   SortUtils.toJpaSort(pageable, ARCHIVED_SORT_MAP, ARCHIVED_DEFAULT_SORT, "id"));
        return repository.findArchived(pageableWithSort);
    }
}
