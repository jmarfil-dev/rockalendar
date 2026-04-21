package com.jmarfildev.rockalendar.moderation.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.events.api.dto.EventCommentDto;
import com.jmarfildev.rockalendar.events.application.EventCommentCommandService;
import com.jmarfildev.rockalendar.events.application.EventCommentQueryService;

/**
 * @author jmarfil
 */
@RestController
@RequiredArgsConstructor
public class ModerationCommentController implements ModerationCommentApi {

    private final EventCommentQueryService queryService;
    private final EventCommentCommandService commandService;

    @Override
    public List<EventCommentDto> listComments(UUID eventId) {
        return queryService.listByEvent(eventId);
    }

    @Override
    public void deleteComment(UUID eventId, UUID commentId) {
        commandService.deleteComment(eventId, commentId);
    }
}
