package com.jmarfildev.rockalendar.events.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmarfildev.rockalendar.events.domain.EventComment;

/**
 * @author jmarfil
 */
public interface EventCommentRepository extends JpaRepository<EventComment, UUID> {

    List<EventComment> findByEventIdOrderByCreatedAtAsc(UUID eventId);
}
