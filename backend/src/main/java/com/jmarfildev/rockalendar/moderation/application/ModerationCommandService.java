package com.jmarfildev.rockalendar.moderation.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapper;
import com.jmarfildev.rockalendar.events.application.EventCommandService;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStateMachine;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApproveRequest;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchiveRequest;
import com.jmarfildev.rockalendar.moderation.domain.ActionType;
import com.jmarfildev.rockalendar.moderation.domain.ModerationAction;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationActionRepository;
import com.jmarfildev.rockalendar.users.application.TrustScoreService;

/**
 * @author jmarfil
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationCommandService {
    private final EventRepository eventRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final EventMapper eventMapper;
    private final CurrentUser currentUser;
    private final TrustScoreService trustScoreService;
    private final EventCommandService eventCommandService;

    @Transactional
    public EventPrivateDto approve(UUID eventId, ModerationApproveRequest request) {
        String comment = request != null ? StringUtils.blankToNull(request.comment()) : null;
        return moderate(eventId, EventStatus.APPROVED, ActionType.APPROVE, comment);
    }

    @Transactional
    public EventPrivateDto reject(UUID eventId, ModerationArchiveRequest request) {
        return moderateWithReason(eventId, EventStatus.REJECTED, ActionType.REJECT, request.reason());
    }

    @Transactional
    public EventPrivateDto hide(UUID eventId, ModerationArchiveRequest request) {
        return moderateWithReason(eventId, EventStatus.HIDDEN, ActionType.HIDE, request.reason());
    }

    @Transactional
    public EventPrivateDto requestChanges(UUID eventId, ModerationArchiveRequest request) {
        long priorChanges = moderationActionRepository.countByEventIdAndAction(eventId, ActionType.REQUEST_CHANGES);
        if (priorChanges >= 2) {
            // Tercera solicitud: rechazo automático con penalización máxima
            String reason = "Rechazado automáticamente tras tres solicitudes de cambios.";
            return moderate(eventId, EventStatus.REJECTED, ActionType.AUTO_REJECT, reason);
        }
        return moderateWithReason(eventId, EventStatus.NEEDS_CHANGES, ActionType.REQUEST_CHANGES, request.reason());
    }

    @Transactional
    public EventPrivateDto editData(UUID eventId, SubmitEventRequest req, MultipartFile poster, boolean removePoster) {
        UUID moderatorId = currentUser.userId();

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.PENDING_MODERATION) {
            throw new ConflictException(ErrorConstants.EVENT_NOT_PENDING, ErrorConstants.TYPE_409_MODERATION_STATE);
        }
        if (moderatorId.equals(event.getCreatedByUserId())) {
            throw new ConflictException(ErrorConstants.MODERATOR_OWN, ErrorConstants.TYPE_409_MODERATION_STATE);
        }

        ModerationAction action = new ModerationAction();
        action.setEventId(eventId);
        action.setAction(ActionType.MODERATOR_EDITED);
        action.setModeratedByUserId(moderatorId);
        action.setCreatedAt(OffsetDateTime.now());

        try {
            EventPrivateDto updated = eventCommandService.moderatorEditData(eventId, req, poster, removePoster);
            moderationActionRepository.saveAndFlush(action);
            return updated;
        }
        catch (ObjectOptimisticLockingFailureException
                | OptimisticLockException
                | StaleObjectStateException e) {
            throw new ConflictException(ErrorConstants.EVENT_ALREADY_MOD, ErrorConstants.TYPE_409_MODERATION_STATE);
        }
    }

    private EventPrivateDto moderateWithReason(UUID eventId, EventStatus targetStatus, ActionType actionType,
                                               String requestReason) {
        String reason = StringUtils.blankToNull(requestReason);
        if (reason == null) {
            throw new BadRequestException(ErrorConstants.REASON_REQUIRED, ErrorConstants.TYPE_400_VALIDATION);
        }
        return moderate(eventId, targetStatus, actionType, reason);
    }

    private void applyTrustScore(ActionType actionType, UUID creatorId, UUID eventId) {
        if (actionType == ActionType.APPROVE) {
            trustScoreService.onApprove(creatorId, eventId);
        } else if (actionType == ActionType.REJECT) {
            trustScoreService.onReject(creatorId, eventId);
        } else if (actionType == ActionType.AUTO_REJECT) {
            trustScoreService.onRejectAfterThirdChange(creatorId, eventId);
        }
        // REQUEST_CHANGES, HIDE y los nuevos tipos no modifican el trust score aquí
    }

    private EventPrivateDto moderate(UUID eventId, EventStatus targetStatus, ActionType actionType, String message) {
        UUID moderatorId = currentUser.userId();
        OffsetDateTime now = OffsetDateTime.now();
        log.info("moderation action={} eventId={} moderatorId={}", actionType.name(), eventId, moderatorId);

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));

        if (!EventStateMachine.canModeratorTransition(event.getStatus(), targetStatus)) {
            throw new ConflictException(ErrorConstants.EVENT_NOT_PENDING, ErrorConstants.TYPE_409_MODERATION_STATE);
        }
        if (moderatorId.equals(event.getCreatedByUserId())) {
            throw new ConflictException(ErrorConstants.MODERATOR_OWN, ErrorConstants.TYPE_409_MODERATION_STATE);
        }

        event.setStatus(targetStatus);
        event.setModeratedByUserId(moderatorId);
        event.setModeratedAt(now);
        event.setModerationMessage(message);

        ModerationAction action = new ModerationAction();
        action.setEventId(eventId);
        action.setAction(actionType);
        action.setReason(message);
        action.setModeratedByUserId(moderatorId);
        action.setCreatedAt(now);

        try {
            moderationActionRepository.saveAndFlush(action);
            applyTrustScore(actionType, event.getCreatedByUserId(), eventId);
            return eventMapper.toPrivateDto(event);
        }
        catch (ObjectOptimisticLockingFailureException
                | OptimisticLockException
                | StaleObjectStateException e) {
            throw new ConflictException(ErrorConstants.EVENT_ALREADY_MOD, ErrorConstants.TYPE_409_MODERATION_STATE);
        }
    }
}
