package com.jmarfildev.rockalendar.moderation.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingDto;
import com.jmarfildev.rockalendar.moderation.application.ModerationQueryService;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
public class ModerationController implements ModerationApi {

    private final ModerationQueryService queryService;

    @Override
    public Page<ModerationPendingDto> listPending(Pageable pageable) {
        return queryService.listPending(pageable);
    }

    @Override
    public Page<ModerationArchivedDto> listArchived(Pageable pageable) {
        return queryService.listArchived(pageable);
    }

}
