package com.jmarfildev.rockalendar.moderation.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.jmarfildev.rockalendar.events.domain.Event;

/**
 * @author jmarfil
 *
 */
public interface ModerationEventRepository extends Repository<Event, UUID> {

    @Query(value = """
                    SELECT
                      e.id            AS id,
                      e.title         AS title,
                      e.submitted_at  AS submittedAt
                    FROM events e
                    WHERE e.status = 'PENDING_MODERATION'
                    ORDER BY
                      e.submitted_at ASC NULLS LAST,
                      e.created_at ASC,
                      e.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM events e
                    WHERE e.status = 'PENDING_MODERATION'
                    """,
            nativeQuery = true)
    Page<ModerationPendingProjection> findPending(Pageable pageable);

    @Query(value = """
                    SELECT
                      e.id                 AS id,
                      e.title              AS title,
                      e.status             AS status,
                      e.moderation_message AS moderationMessage,
                      e.moderated_at       AS moderatedAt
                    FROM events e
                    WHERE e.status IN ('REJECTED', 'HIDDEN', 'CANCELED')
                    ORDER BY
                      e.moderated_at DESC NULLS LAST,
                      e.updated_at DESC,
                      e.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM events e
                    WHERE e.status IN ('REJECTED', 'HIDDEN', 'CANCELED')
                    """,
            nativeQuery = true)
    Page<ModerationArchivedProjection> findArchived(Pageable pageable);
}
