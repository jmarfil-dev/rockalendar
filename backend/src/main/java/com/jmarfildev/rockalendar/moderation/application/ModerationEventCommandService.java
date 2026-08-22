package com.jmarfildev.rockalendar.moderation.application;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.StringUtils;
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
import com.jmarfildev.rockalendar.notifications.application.NotificationService;
import com.jmarfildev.rockalendar.notifications.domain.NotificationType;
import com.jmarfildev.rockalendar.users.application.TrustScoreService;

/**
 * @author jmarfil
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationEventCommandService {
    private final EventRepository eventRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final EventMapper eventMapper;
    private final CurrentUser currentUser;
    private final TrustScoreService trustScoreService;
    private final EventCommandService eventCommandService;
    private final NotificationService notificationService;

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
    public EventPrivateDto edit(UUID eventId, SubmitEventRequest req, MultipartFile poster, boolean removePoster) {
        UUID moderatorId = currentUser.userId();
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.PENDING_MODERATION && event.getStatus() != EventStatus.FLAGGED) {
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
            // El moderador nunca puede marcar la fecha como no confirmada (dateTbd): solo el admin lo expone en su API.
            EventPrivateDto updated = eventCommandService.moderatorEdit(moderatorId, eventId, req, poster, removePoster, false);
            moderationActionRepository.saveAndFlush(action);
            log.info("moderator edited event data eventId={} moderatorId={}", eventId, moderatorId);
            return updated;
        }
        catch (ObjectOptimisticLockingFailureException | OptimisticLockException | StaleObjectStateException e) {
            throw new ConflictException(ErrorConstants.EVENT_ALREADY_MOD, ErrorConstants.TYPE_409_MODERATION_STATE);
        }
    }

    private EventPrivateDto moderateWithReason(UUID eventId, EventStatus targetStatus, ActionType actionType, String requestReason) {
        String reason = StringUtils.blankToNull(requestReason);
        if (reason == null) {
            throw new BadRequestException(ErrorConstants.REASON_REQUIRED, ErrorConstants.TYPE_400_VALIDATION);
        }
        return moderate(eventId, targetStatus, actionType, reason);
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

        // Al aprobar un duplicado: limpiar su referencia y marcar el original si sigue revisable
        if (targetStatus == EventStatus.APPROVED && event.getPossibleDuplicateOf() != null) {
            UUID originalId = event.getPossibleDuplicateOf();
            event.setPossibleDuplicateOf(null);
            eventRepository.findById(originalId).ifPresent(original -> {
                EventStatus s = original.getStatus();
                if (s == EventStatus.PENDING_MODERATION || s == EventStatus.NEEDS_CHANGES || s == EventStatus.FLAGGED) {
                    original.setPossibleDuplicateOf(event.getId());
                    log.info("original event marked as duplicate of newly approved eventId={} originalId={}", event.getId(), originalId);
                }
            });
        }

        ModerationAction action = new ModerationAction();
        action.setEventId(eventId);
        action.setAction(actionType);
        action.setReason(message);
        action.setModeratedByUserId(moderatorId);
        action.setCreatedAt(now);

        try {
            moderationActionRepository.saveAndFlush(action);
            // Comprobar autoban solo tras acciones que penalizan fuertemente
            if (actionType == ActionType.REJECT || actionType == ActionType.AUTO_REJECT) {
                trustScoreService.checkAutoban(event.getCreatedByUserId());
            }
            notifyOwner(actionType, event, message);
            return eventMapper.toPrivateDto(event);
        }
        catch (ObjectOptimisticLockingFailureException | OptimisticLockException | StaleObjectStateException e) {
            throw new ConflictException(ErrorConstants.EVENT_ALREADY_MOD, ErrorConstants.TYPE_409_MODERATION_STATE);
        }
    }

    private void notifyOwner(ActionType actionType, Event event, String message) {
        UUID recipientId = event.getCreatedByUserId();
        UUID eventId = event.getId();
        String reason = message != null ? message : "";
        Map<String, String> payload =
                Map.of(NotificationService.PAYLOAD_TITLE, event.getTitle(), NotificationService.PAYLOAD_REASON, reason);
        switch (actionType) {
            case APPROVE -> notificationService.notifyUser(recipientId, NotificationType.EVENT_APPROVED, eventId, payload);
            case REJECT, AUTO_REJECT -> notificationService.notifyUser(recipientId, NotificationType.EVENT_REJECTED, eventId, payload);
            case REQUEST_CHANGES -> notificationService.notifyUser(recipientId, NotificationType.EVENT_NEEDS_CHANGES, eventId, payload);
            default -> { /* HIDE y MODERATOR_EDITED no generan notificación al autor */ }
        }
    }
}
