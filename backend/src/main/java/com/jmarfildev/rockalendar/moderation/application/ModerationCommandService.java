package com.jmarfildev.rockalendar.moderation.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.common.helper.StringUtils;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapper;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApproveRequest;
import com.jmarfildev.rockalendar.moderation.domain.ActionType;
import com.jmarfildev.rockalendar.moderation.domain.ModerationAction;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationActionRepository;

/**
 * @author jmarfil
 *
 */
@Service
@RequiredArgsConstructor
public class ModerationCommandService {
    private final EventRepository eventRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final EventMapper eventMapper;
    private final CurrentUser currentUser;

    @Transactional
    public EventPrivateDto approve(UUID eventId, ModerationApproveRequest request) {
        UUID moderatorId = currentUser.userId();
        String message = request != null ? StringUtils.blankToNull(request.reason()) : null;
        OffsetDateTime now = OffsetDateTime.now();

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(ErrorMessages.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.PENDING_MODERATION) {
            throw new ConflictException(ErrorMessages.EVENT_NOT_PENDING, ErrorMessages.TYPE_MODERATION_STATE);
        }
        if (moderatorId.equals(event.getCreatedByUserId())) {
            throw new ConflictException(ErrorMessages.MOERATOR_OWN, ErrorMessages.TYPE_MODERATION_STATE);
        }

        event.setStatus(EventStatus.APPROVED);
        event.setModeratedByUserId(moderatorId);
        event.setModeratedAt(now);
        event.setModerationMessage(message);

        ModerationAction action = new ModerationAction();
        action.setEventId(eventId);
        action.setAction(ActionType.APPROVE);
        action.setReason(message);
        action.setModeratedByUserId(moderatorId);
        action.setCreatedAt(now);

        try {
            moderationActionRepository.saveAndFlush(action);
            return eventMapper.toPrivateDto(event);
        }
        catch (ObjectOptimisticLockingFailureException // Excepción de spring
                | OptimisticLockException // Excepción de java (jakarta.persistence)
                | StaleObjectStateException e) { // Excepción de hibernate
            // otro moderador lo tocó entre lectura y commit
            throw new ConflictException(ErrorMessages.EVENT_ALREADY_MOD, ErrorMessages.TYPE_MODERATION_STATE);
        }
    }
}
