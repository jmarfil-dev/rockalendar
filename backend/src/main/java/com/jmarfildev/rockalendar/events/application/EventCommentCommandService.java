package com.jmarfildev.rockalendar.events.application;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.events.api.dto.PostCommentRequest;
import com.jmarfildev.rockalendar.events.domain.EventComment;
import com.jmarfildev.rockalendar.events.persistence.EventCommentRepository;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.notifications.application.NotificationService;
import com.jmarfildev.rockalendar.notifications.domain.NotificationType;

/**
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
public class EventCommentCommandService {

    private final EventRepository eventRepository;
    private final EventCommentRepository commentRepository;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    @Transactional
    public void postComment(UUID eventId, PostCommentRequest request) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));

        String authorEmail;
        UUID authorUserId = null;

        var userId = currentUser.tryGetUserId();
        if (userId.isPresent()) {
            authorUserId = userId.get();
            authorEmail = currentUser.tryGetEmail()
                    .orElseThrow(() -> new BadRequestException(ErrorConstants.REQUEST_REQUIRED));
        } else {
            if (request.authorEmail() == null || request.authorEmail().isBlank()) {
                throw new BadRequestException(ErrorConstants.REQUEST_REQUIRED);
            }
            authorEmail = request.authorEmail();
        }

        var comment = EventComment.builder()
                .eventId(eventId)
                .authorUserId(authorUserId)
                .authorEmail(authorEmail)
                .authorName(request.authorName())
                .body(request.body())
                .build();

        commentRepository.save(comment);

        var payload = Map.of(
                NotificationService.PAYLOAD_TITLE, event.getTitle(),
                NotificationService.PAYLOAD_PREVIEW, request.body().substring(0, Math.min(100, request.body().length()))
        );
        notificationService.notifyAllModerators(NotificationType.EVENT_COMMENT, eventId, payload);
    }

    @Transactional
    public void deleteComment(UUID eventId, UUID commentId) {
        EventComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));
        if (!comment.getEventId().equals(eventId)) {
            throw new NotFoundException(ErrorConstants.EVENT_NOT_FOUND);
        }
        commentRepository.delete(comment);
    }
}
