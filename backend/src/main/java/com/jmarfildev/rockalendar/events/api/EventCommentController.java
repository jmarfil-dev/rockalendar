package com.jmarfildev.rockalendar.events.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.events.api.dto.PostCommentRequest;
import com.jmarfildev.rockalendar.events.application.EventCommentCommandService;

/**
 * @author jmarfil
 */
@RestController
@RequiredArgsConstructor
public class EventCommentController implements EventCommentApi {

    private final EventCommentCommandService commandService;

    @Override
    public void postComment(UUID id, PostCommentRequest request) {
        commandService.postComment(id, request);
    }
}
