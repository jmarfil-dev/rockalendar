package com.jmarfildev.rockalendar.admin.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.admin.api.dto.AdminStatusOverrideRequest;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapper;
import com.jmarfildev.rockalendar.events.application.EventCommandService;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStateMachine;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.moderation.domain.ActionType;
import com.jmarfildev.rockalendar.moderation.domain.ModerationAction;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationActionRepository;

/**
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEventCommandService {

    private final EventRepository eventRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final EventMapper eventMapper;
    private final CurrentUser currentUser;
    private final EventCommandService eventCommandService;

    @Transactional
    public EventPrivateDto overrideStatus(UUID eventId, AdminStatusOverrideRequest request) {
        UUID adminId = currentUser.userId();
        OffsetDateTime now = OffsetDateTime.now();
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));

        if (!EventStateMachine.canAdminTransition(event.getStatus(), request.targetStatus())) {
            throw new ConflictException(ErrorConstants.EVENT_ERASED_TERMINAL, ErrorConstants.TYPE_409_EVENT_STATE);
        }

        event.setStatus(request.targetStatus());
        event.setModeratedByUserId(adminId);
        event.setModeratedAt(now);
        event.setModerationMessage(request.reason());

        ModerationAction action = new ModerationAction();
        action.setEventId(eventId);
        action.setAction(ActionType.ADMIN_STATE_OVERRIDE);
        action.setReason(request.reason());
        action.setModeratedByUserId(adminId);
        action.setCreatedAt(now);

        moderationActionRepository.save(action);

        log.info("admin status override eventId={} from={} to={} adminId={}", eventId, event.getStatus(), request.targetStatus(), adminId);
        return eventMapper.toPrivateDto(event);
    }

    @Transactional
    public EventPrivateDto edit(UUID eventId,
                                SubmitEventRequest req,
                                MultipartFile poster,
                                boolean removePoster,
                                String comment,
                                boolean dateTbd) {
        UUID adminId = currentUser.userId();
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));

        if (!EventStateMachine.canAdminEdit(event.getStatus())) {
            throw new ConflictException(ErrorConstants.EVENT_NOT_EDITABLE, ErrorConstants.TYPE_409_EVENT_STATE);
        }

        ModerationAction action = new ModerationAction();
        action.setEventId(eventId);
        action.setAction(ActionType.ADMIN_EDITED);
        action.setReason(comment);
        action.setModeratedByUserId(adminId);
        action.setCreatedAt(OffsetDateTime.now());

        try {
            EventPrivateDto updated = eventCommandService.moderatorEdit(adminId, eventId, req, poster, removePoster, dateTbd);
            moderationActionRepository.saveAndFlush(action);
            log.info("admin edited event data eventId={} adminId={}", eventId, adminId);
            return updated;
        }
        catch (ObjectOptimisticLockingFailureException | OptimisticLockException | StaleObjectStateException e) {
            throw new ConflictException(ErrorConstants.EVENT_ALREADY_MOD, ErrorConstants.TYPE_409_MODERATION_STATE);
        }
    }
}
