package com.jmarfildev.rockalendar.events.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.events.api.dto.EventCommentDto;
import com.jmarfildev.rockalendar.events.domain.EventComment;
import com.jmarfildev.rockalendar.events.persistence.EventCommentRepository;

/**
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
public class EventCommentQueryService {

    private final EventCommentRepository commentRepository;

    public List<EventCommentDto> listByEvent(UUID eventId) {
        return commentRepository.findByEventIdOrderByCreatedAtAsc(eventId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private EventCommentDto toDto(EventComment c) {
        return new EventCommentDto(c.getId(), c.getAuthorEmail(), c.getAuthorName(), c.getBody(), c.getCreatedAt());
    }
}
