package com.jmarfildev.rockalendar.moderation.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.CommonValidations;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingDto;
import com.jmarfildev.rockalendar.moderation.api.mapper.ModerationMapper;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationRepository;

/**
 * @author jmarfil
 *
 */
@Service
@RequiredArgsConstructor
public class ModerationQueryService {

    private final ModerationRepository repository;
    private final ModerationMapper mapper;

    @Transactional(readOnly = true)
    public Page<ModerationPendingDto> listPending(Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        return repository.findPending(pageable).map(mapper::toPendingDto);
    }

    @Transactional(readOnly = true)
    public Page<ModerationArchivedDto> listArchived(Pageable pageable) {
        CommonValidations.validatePageable(pageable);
        return repository.findArchived(pageable).map(mapper::toArchivedDto);
    }
}
