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
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapper;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApproveRequest;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchiveRequest;
import com.jmarfildev.rockalendar.moderation.domain.ActionType;
import com.jmarfildev.rockalendar.moderation.domain.ModerationAction;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationActionRepository;

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

    @Transactional
    public EventPrivateDto approve(UUID eventId, ModerationApproveRequest request) {
        String comment = request != null ? StringUtils.blankToNull(request.comment()) : null;
        return moderate(eventId, ActionType.APPROVE, comment, (event, moderatorId, now, message) -> {
            event.setStatus(EventStatus.APPROVED);
            event.setModeratedByUserId(moderatorId);
            event.setModeratedAt(now);
            event.setModerationMessage(message);
        });
    }

    @Transactional
    public EventPrivateDto reject(UUID eventId, ModerationArchiveRequest request) {
        return archive(eventId, request.reason(), ActionType.REJECT, EventStatus.REJECTED);
    }

    @Transactional
    public EventPrivateDto hide(UUID eventId, ModerationArchiveRequest request) {
        return archive(eventId, request.reason(), ActionType.HIDE, EventStatus.HIDDEN);
    }

    @Transactional
    public EventPrivateDto requestChanges(UUID eventId, ModerationArchiveRequest request) {
        return archive(eventId, request.reason(), ActionType.REQUEST_CHANGES, EventStatus.NEEDS_CHANGES);
    }

    private EventPrivateDto archive(UUID eventId, String requestReason, ActionType action, EventStatus status) {
        String reason = StringUtils.blankToNull(requestReason);
        if (reason == null) {
            throw new BadRequestException(ErrorConstants.REASON_REQUIRED, ErrorConstants.TYPE_400_VALIDATION);
        }
        return moderate(eventId, action, reason, (event, moderatorId, now, msg) -> {
            event.setStatus(status);
            event.setModeratedByUserId(moderatorId);
            event.setModeratedAt(now);
            event.setModerationMessage(msg);
        });
    }

    @FunctionalInterface
    private interface EventModerationMutation {
        void apply(Event event, UUID moderatorId, OffsetDateTime now, String message);
    }

    private EventPrivateDto moderate(UUID eventId,
                                     ActionType actionType,
                                     String message,
                                     EventModerationMutation mutation) {
        UUID moderatorId = currentUser.userId();
        OffsetDateTime now = OffsetDateTime.now();
        log.info("moderation action={} eventId={} moderatorId={}", actionType.name(), eventId, moderatorId);

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.PENDING_MODERATION) {
            throw new ConflictException(ErrorConstants.EVENT_NOT_PENDING, ErrorConstants.TYPE_409_MODERATION_STATE);
        }
        if (moderatorId.equals(event.getCreatedByUserId())) {
            throw new ConflictException(ErrorConstants.MODERATOR_OWN, ErrorConstants.TYPE_409_MODERATION_STATE);
        }

        mutation.apply(event, moderatorId, now, message);

        ModerationAction action = new ModerationAction();
        action.setEventId(eventId);
        action.setAction(actionType);
        action.setReason(message);
        action.setModeratedByUserId(moderatorId);
        action.setCreatedAt(now);

        try {
            moderationActionRepository.saveAndFlush(action);
            return eventMapper.toPrivateDto(event);
        }
        catch (ObjectOptimisticLockingFailureException
                | OptimisticLockException
                | StaleObjectStateException e) {
            throw new ConflictException(ErrorConstants.EVENT_ALREADY_MOD, ErrorConstants.TYPE_409_MODERATION_STATE);
        }
    }
}
