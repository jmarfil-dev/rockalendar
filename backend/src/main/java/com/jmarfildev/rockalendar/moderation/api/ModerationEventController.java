package com.jmarfildev.rockalendar.moderation.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApproveRequest;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchiveRequest;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingDto;
import com.jmarfildev.rockalendar.moderation.application.ModerationCommandService;
import com.jmarfildev.rockalendar.moderation.application.ModerationQueryService;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
public class ModerationEventController implements ModerationEventApi {

    private final ModerationQueryService queryService;
    private final ModerationCommandService commandService;

    @Override
    public Page<ModerationPendingDto> listPending(Pageable pageable) {
        return queryService.listPending(pageable);
    }

    @Override
    public Page<ModerationArchivedDto> listArchived(Pageable pageable) {
        return queryService.listArchived(pageable);
    }

    @Override
    public EventPrivateDto approve(UUID eventId, @Valid ModerationApproveRequest request) {
        return commandService.approve(eventId, request);
    }

    @Override
    public EventPrivateDto reject(UUID eventId, @Valid ModerationArchiveRequest request) {
        return commandService.reject(eventId, request);
    }

    @Override
    public EventPrivateDto hide(UUID eventId, @Valid ModerationArchiveRequest request) {
        return commandService.hide(eventId, request);
    }

    @Override
    public EventPrivateDto requestChanges(UUID eventId, @Valid ModerationArchiveRequest request) {
        return commandService.requestChanges(eventId, request);
    }
}
