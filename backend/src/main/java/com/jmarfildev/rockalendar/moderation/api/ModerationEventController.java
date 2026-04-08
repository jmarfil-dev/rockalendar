package com.jmarfildev.rockalendar.moderation.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.web.multipart.MultipartFile;

import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApproveRequest;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApprovedListItemDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchiveRequest;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedListItemDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingListItemDto;
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
    public EventPrivateDto getForModeration(UUID eventId) {
        return queryService.getForModeration(eventId);
    }

    @Override
    public Page<ModerationPendingListItemDto> listPending(Pageable pageable) {
        return queryService.listPending(pageable);
    }

    @Override
    public Page<ModerationApprovedListItemDto> listApproved(Pageable pageable) {
        return queryService.listApproved(pageable);
    }

    @Override
    public Page<ModerationArchivedListItemDto> listArchived(Pageable pageable) {
        return queryService.listArchived(pageable);
    }

    @Override
    public EventPrivateDto edit(UUID eventId, SubmitEventRequest request, MultipartFile poster, boolean removePoster) {
        return commandService.edit(eventId, request, poster, removePoster);
    }

    @Override
    public EventPrivateDto approve(UUID eventId, ModerationApproveRequest request) {
        return commandService.approve(eventId, request);
    }

    @Override
    public EventPrivateDto reject(UUID eventId, ModerationArchiveRequest request) {
        return commandService.reject(eventId, request);
    }

    @Override
    public EventPrivateDto hide(UUID eventId, ModerationArchiveRequest request) {
        return commandService.hide(eventId, request);
    }

    @Override
    public EventPrivateDto requestChanges(UUID eventId, ModerationArchiveRequest request) {
        return commandService.requestChanges(eventId, request);
    }
}
