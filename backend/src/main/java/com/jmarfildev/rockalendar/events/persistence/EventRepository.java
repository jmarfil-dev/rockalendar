package com.jmarfildev.rockalendar.events.persistence;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicListItemDto;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 *
 */
public interface EventRepository extends JpaRepository<Event, UUID> {

    @EntityGraph(attributePaths = { "province", "artists" })
    Optional<Event> findByIdAndStatus(UUID id, EventStatus status);

    @EntityGraph(attributePaths = { "province", "artists" })
    Optional<Event> findByTitleAndStatus(String title, EventStatus status);

    @Query("""
                SELECT new com.jmarfildev.rockalendar.events.api.dto.EventPublicListItemDto(
                    e.id,
                    e.title,
                    e.startDateTime,
                    e.endDateTime,
                    p.name,
                    e.cityName
                )
                FROM Event e
                JOIN e.province p
                WHERE e.status = 'APPROVED'
                    AND (
                        (e.endDateTime IS NOT NULL AND e.endDateTime >= CURRENT_TIMESTAMP)
                            OR (e.endDateTime IS NULL AND e.startDateTime >= CURRENT_TIMESTAMP)
                    )
            """)
    Page<EventPublicListItemDto> findHome(Pageable pageable);

    @Query("""
                SELECT new com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto(
                    e.id, e.title, e.startDateTime, p.name, e.cityName, e.status, e.moderationMessage, e.createdAt
                )
                FROM Event e
                JOIN e.province p
                WHERE e.createdByUserId = :userId
                  AND e.status = :status
            """)
    Page<EventPrivateListItemDto> listMineByStatus(UUID userId, EventStatus status, Pageable pageable);

    @Query("""
                SELECT new com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto(
                    e.id, e.title, e.startDateTime, p.name, e.cityName, e.status, e.moderationMessage, e.createdAt
                )
                FROM Event e
                JOIN e.province p
                WHERE e.createdByUserId = :userId
                  AND e.status NOT IN :excluded
            """)
    Page<EventPrivateListItemDto> listMineExcludingStatuses(UUID userId, Collection<EventStatus> excluded, Pageable pageable);

    /**
     * 1º NEED_CHANGES, 2º PENDING_MODERATION, 3º resto y dentro de cada grupo: futuros primero (cercanos) y pasados después (recientes)
     *
     * @param userId
     * @param pageable
     * @return
     */
    @Query("""
                SELECT new com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto(
                    e.id, e.title, e.startDateTime, p.name, e.cityName, e.status, e.moderationMessage, e.createdAt
                )
                FROM Event e
                JOIN e.province p
                WHERE e.createdByUserId = :userId
                ORDER BY
                  CASE
                    WHEN e.status = 'NEEDS_CHANGES' THEN 0
                    WHEN e.status = 'PENDING_MODERATION' THEN 1
                    ELSE 2
                  END ASC,
                  e.status ASC,
                  CASE WHEN e.startDateTime >= CURRENT_TIMESTAMP THEN 0 ELSE 1 END ASC,
                  CASE WHEN e.startDateTime >= CURRENT_TIMESTAMP THEN e.startDateTime END ASC,
                  CASE WHEN e.startDateTime <  CURRENT_TIMESTAMP THEN e.startDateTime END DESC
            """)
    Page<EventPrivateListItemDto> listMineAllPriorityFutureFirst(UUID userId, Pageable pageable);
}
