package com.jmarfildev.rockalendar.events.persistence;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Sobreescribe findById de JpaRepository para cargar province y artists con JOIN FETCH
    @EntityGraph(attributePaths = { "province", "artists" })
    Optional<Event> findById(UUID id);

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
                WHERE e.status = com.jmarfildev.rockalendar.events.domain.EventStatus.APPROVED
                    AND (
                        (e.endDateTime IS NOT NULL AND e.endDateTime >= CURRENT_TIMESTAMP)
                            OR (e.endDateTime IS NULL AND e.startDateTime >= CURRENT_TIMESTAMP)
                    )
            """)
    Page<EventPublicListItemDto> findHome(Pageable pageable);

    @Query("""
                SELECT new com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto(
                    e.id, e.title, e.startDateTime, p.name, e.cityName, e.status, e.moderationMessage, e.submittedAt
                )
                FROM Event e
                JOIN e.province p
                WHERE e.createdByUserId = :userId
                  AND e.status = :status
            """)
    Page<EventPrivateListItemDto> listMineByStatus(UUID userId, EventStatus status, Pageable pageable);

    @Query("""
                SELECT new com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto(
                    e.id, e.title, e.startDateTime, p.name, e.cityName, e.status, e.moderationMessage, e.submittedAt
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
                    e.id, e.title, e.startDateTime, p.name, e.cityName, e.status, e.moderationMessage, e.submittedAt
                )
                FROM Event e
                JOIN e.province p
                WHERE e.createdByUserId = :userId
                ORDER BY
                  CASE
                   WHEN e.status = com.jmarfildev.rockalendar.events.domain.EventStatus.NEEDS_CHANGES THEN 0
                   WHEN e.status = com.jmarfildev.rockalendar.events.domain.EventStatus.PENDING_MODERATION THEN 1
                   WHEN e.status = com.jmarfildev.rockalendar.events.domain.EventStatus.REJECTED THEN 2
                   ELSE 3
                  END ASC,
                  e.status ASC,
                  CASE WHEN e.startDateTime >= CURRENT_TIMESTAMP THEN 0 ELSE 1 END ASC,
                  CASE WHEN e.startDateTime >= CURRENT_TIMESTAMP THEN e.startDateTime END ASC,
                  CASE WHEN e.startDateTime <  CURRENT_TIMESTAMP THEN e.startDateTime END DESC
            """)
    Page<EventPrivateListItemDto> listMineAllPriorityFutureFirst(UUID userId, Pageable pageable);

    @Query(value = """
                   SELECT
                     e.id              AS id,
                     e.title           AS title,
                     e.start_date_time AS startDateTime,
                     e.end_date_time   AS endDateTime,
                     p.name            AS provinceName,
                     e.city_name       AS cityName,
                     s.score           AS score
                   FROM search_public_events(
                     :q, :minSim, :ftsW, :trgmW,
                     :dateFrom, :dateTo,
                     :provinceId, :citySlug, :artistSlug
                   ) s
                   JOIN events e ON e.id = s.event_id
                   JOIN provinces p ON p.id = e.province_id
                   ORDER BY
                     -- relevancia solo si se pide explícitamente
                     CASE WHEN lower(:sortKey) = 'relevance' THEN s.score END DESC,

                     -- ASC
                     CASE WHEN lower(:sortDir) = 'asc'  AND lower(:sortKey) = 'date'     THEN e.start_date_time END ASC,
                     CASE WHEN lower(:sortDir) = 'asc'  AND lower(:sortKey) = 'title'    THEN e.title          END ASC,
                     CASE WHEN lower(:sortDir) = 'asc'  AND lower(:sortKey) = 'province' THEN p.name           END ASC,
                     CASE WHEN lower(:sortDir) = 'asc'  AND lower(:sortKey) = 'city'     THEN e.city_name      END ASC,

                     -- DESC
                     CASE WHEN lower(:sortDir) = 'desc' AND lower(:sortKey) = 'date'     THEN e.start_date_time END DESC,
                     CASE WHEN lower(:sortDir) = 'desc' AND lower(:sortKey) = 'title'    THEN e.title          END DESC,
                     CASE WHEN lower(:sortDir) = 'desc' AND lower(:sortKey) = 'province' THEN p.name           END DESC,
                     CASE WHEN lower(:sortDir) = 'desc' AND lower(:sortKey) = 'city'     THEN e.city_name      END DESC,

                     e.id ASC
                   """, countQuery = """
                                     SELECT COUNT(*)
                                     FROM search_public_events(
                                       :q, :minSim, :ftsW, :trgmW,
                                       :dateFrom, :dateTo,
                                       :provinceId, :citySlug, :artistSlug
                                     ) s
                                     """, nativeQuery = true)
    Page<EventPublicSearchProjection> searchPublicEvents(@Param("q") String q,
                                                         @Param("minSim") double minSim,
                                                         @Param("ftsW") double ftsW,
                                                         @Param("trgmW") double trgmW,
                                                         @Param("dateFrom") OffsetDateTime dateFrom,
                                                         @Param("dateTo") OffsetDateTime dateTo,
                                                         @Param("provinceId") UUID provinceId,
                                                         @Param("citySlug") String citySlug,
                                                         @Param("artistSlug") UUID artistId,
                                                         @Param("sortKey") String sortKey,
                                                         @Param("sortDir") String sortDir,
                                                         Pageable pageable);

    @Query(value = """
                   SELECT
                     e.id              AS id,
                     e.title           AS title,
                     e.start_date_time AS startDateTime,
                     e.end_date_time   AS endDateTime,
                     p.name            AS provinceName,
                     e.city_name       AS cityName,
                     s.score           AS score
                   FROM search_public_events_fallback(
                     :q, :minSim, :ftsW, :trgmW,
                     :dateFrom, :dateTo,
                     :provinceId, :citySlug, :artistSlug
                   ) s
                   JOIN events e ON e.id = s.event_id
                   JOIN provinces p ON p.id = e.province_id
                   ORDER BY
                     -- relevancia solo si se pide explícitamente
                     CASE WHEN lower(:sortKey) = 'relevance' THEN s.score END DESC,

                     CASE WHEN lower(:sortDir) = 'asc'  AND lower(:sortKey) = 'date'     THEN e.start_date_time END ASC,
                     CASE WHEN lower(:sortDir) = 'asc'  AND lower(:sortKey) = 'title'    THEN e.title          END ASC,
                     CASE WHEN lower(:sortDir) = 'asc'  AND lower(:sortKey) = 'province' THEN p.name           END ASC,
                     CASE WHEN lower(:sortDir) = 'asc'  AND lower(:sortKey) = 'city'     THEN e.city_name      END ASC,

                     CASE WHEN lower(:sortDir) = 'desc' AND lower(:sortKey) = 'date'     THEN e.start_date_time END DESC,
                     CASE WHEN lower(:sortDir) = 'desc' AND lower(:sortKey) = 'title'    THEN e.title          END DESC,
                     CASE WHEN lower(:sortDir) = 'desc' AND lower(:sortKey) = 'province' THEN p.name           END DESC,
                     CASE WHEN lower(:sortDir) = 'desc' AND lower(:sortKey) = 'city'     THEN e.city_name      END DESC,

                     e.id ASC
                   """, countQuery = """
                                     SELECT COUNT(*)
                                     FROM search_public_events_fallback(
                                       :q, :minSim, :ftsW, :trgmW,
                                       :dateFrom, :dateTo,
                                       :provinceId, :citySlug, :artistSlug
                                     ) s
                                     """, nativeQuery = true)
    Page<EventPublicSearchProjection> searchPublicEventsFallback(@Param("q") String q,
                                                                 @Param("minSim") double minSim,
                                                                 @Param("ftsW") double ftsW,
                                                                 @Param("trgmW") double trgmW,
                                                                 @Param("dateFrom") OffsetDateTime dateFrom,
                                                                 @Param("dateTo") OffsetDateTime dateTo,
                                                                 @Param("provinceId") UUID provinceId,
                                                                 @Param("citySlug") String citySlug,
                                                                 @Param("artistSlug") UUID artistId,
                                                                 @Param("sortKey") String sortKey,
                                                                 @Param("sortDir") String sortDir,
                                                                 Pageable pageable);
}
