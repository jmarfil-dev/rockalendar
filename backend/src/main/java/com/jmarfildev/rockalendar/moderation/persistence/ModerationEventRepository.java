package com.jmarfildev.rockalendar.moderation.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApprovedListItemDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedListItemDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingListItemDto;

/**
 * @author jmarfil
 *
 */
public interface ModerationEventRepository extends Repository<Event, UUID> {

    @Query("""
           SELECT new com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingListItemDto(
               e.id, e.title, e.submittedAt, e.possibleDuplicateOf
           )
           FROM Event e
           WHERE e.status = com.jmarfildev.rockalendar.events.domain.EventStatus.PENDING_MODERATION
           """)
    Page<ModerationPendingListItemDto> findPending(Pageable pageable);

    @Query("""
           SELECT new com.jmarfildev.rockalendar.moderation.api.dto.ModerationApprovedListItemDto(
               e.id, e.title, e.moderatedAt
           )
           FROM Event e
           WHERE e.status = com.jmarfildev.rockalendar.events.domain.EventStatus.APPROVED
           """)
    Page<ModerationApprovedListItemDto> findApproved(Pageable pageable);

    @Query("""
           SELECT new com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedListItemDto(
               e.id, e.title, e.status, e.moderationMessage, e.moderatedAt
           )
           FROM Event e
           WHERE e.status IN (
               com.jmarfildev.rockalendar.events.domain.EventStatus.REJECTED,
               com.jmarfildev.rockalendar.events.domain.EventStatus.HIDDEN,
               com.jmarfildev.rockalendar.events.domain.EventStatus.CANCELED
           )
           """)
    Page<ModerationArchivedListItemDto> findArchived(Pageable pageable);
}
