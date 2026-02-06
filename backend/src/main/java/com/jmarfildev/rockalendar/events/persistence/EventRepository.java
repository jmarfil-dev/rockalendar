package com.jmarfildev.rockalendar.events.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
                SELECT e
                FROM Event e
                WHERE e.createdByUserId = :userId
                ORDER BY
                  CASE WHEN e.startDateTime >= CURRENT_TIMESTAMP THEN 0 ELSE 1 END ASC,
                  CASE WHEN e.startDateTime >= CURRENT_TIMESTAMP THEN e.startDateTime END ASC,
                  CASE WHEN e.startDateTime <  CURRENT_TIMESTAMP THEN e.startDateTime END DESC
            """)
    @EntityGraph(attributePaths = { "province", "artists" })
    Page<Event> listMineOrderFutureFirst(UUID userId, Pageable pageable);
}
