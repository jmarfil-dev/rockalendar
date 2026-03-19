package com.jmarfildev.rockalendar.events.persistence;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
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
                  AND e.status IN :statuses
            """)
    Page<EventPrivateListItemDto> listMineByStatuses(UUID userId, Collection<EventStatus> statuses, Pageable pageable);

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
                   WHEN e.status = com.jmarfildev.rockalendar.events.domain.EventStatus.FLAGGED THEN 1
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
                     :provinceId, :citySlug, :artistId
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
                                       :provinceId, :citySlug, :artistId
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
                                                         @Param("artistId") UUID artistId,
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
                     :provinceId, :citySlug, :artistId
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
                                       :provinceId, :citySlug, :artistId
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
                                                                 @Param("artistId") UUID artistId,
                                                                 @Param("sortKey") String sortKey,
                                                                 @Param("sortDir") String sortDir,
                                                                 Pageable pageable);

    // --- Detección de posibles duplicados ---

    /**
     * Busca un posible evento duplicado: mismo día, al menos un artista en común,
     * y similitud de venue o título > 0.3 (pg_trgm). Excluye eventos ERASED.
     * Se ejecuta ANTES de guardar el nuevo evento, por lo que no hace falta excluir ningún ID.
     */
    @Query(value = """
                   SELECT DISTINCT e.id AS id, e.title AS title, e.status AS status
                   FROM events e
                   JOIN event_artists ea ON ea.event_id = e.id
                   WHERE e.start_date_time >= :dayStart
                     AND e.start_date_time < :dayEnd
                     AND ea.artist_id IN (:artistIds)
                     AND e.status <> 'ERASED'
                     AND (
                       similarity(e.venue_name, :venueName) > 0.3
                       OR similarity(e.title, :title) > 0.3
                     )
                   LIMIT 1
                   """, nativeQuery = true)
    List<DuplicateEventProjection> findPossibleDuplicate(@Param("dayStart") OffsetDateTime dayStart,
                                                         @Param("dayEnd") OffsetDateTime dayEnd,
                                                         @Param("artistIds") Collection<UUID> artistIds,
                                                         @Param("venueName") String venueName,
                                                         @Param("title") String title);

    // --- Queries para moderación automática ---

    /**
     * Cuenta los eventos rechazados de un usuario desde una fecha dada (para anti-spam).
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.createdByUserId = :userId AND e.status = com.jmarfildev.rockalendar.events.domain.EventStatus.REJECTED AND e.moderatedAt >= :since")
    long countRejectedByUserSince(@Param("userId") UUID userId, @Param("since") OffsetDateTime since);

    /**
     * Devuelve eventos en estado FLAGGED cuyo updatedAt es anterior al umbral (para el scheduler de rechazo).
     */
    List<Event> findByStatusAndUpdatedAtBefore(EventStatus status, OffsetDateTime threshold);

    // --- Queries para comprobación de elegibilidad de ascenso ---

    /**
     * Devuelve venue slugs donde el usuario supera el límite de eventos en el período reciente.
     * Un resultado no vacío indica que el usuario no es elegible.
     */
    @Query("""
                SELECT e.venueSlug
                FROM Event e
                WHERE e.createdByUserId = :userId
                  AND e.createdAt >= :since
                  AND e.status NOT IN :excluded
                GROUP BY e.venueSlug
                HAVING COUNT(e) > :limit
            """)
    List<String> findVenuesExceedingRecentLimit(UUID userId, OffsetDateTime since, Collection<EventStatus> excluded, long limit);

    /**
     * Devuelve artist IDs donde el usuario supera el límite de eventos en el período reciente.
     */
    @Query("""
                SELECT a.id
                FROM Event e
                JOIN e.artists a
                WHERE e.createdByUserId = :userId
                  AND e.createdAt >= :since
                  AND e.status NOT IN :excluded
                GROUP BY a.id
                HAVING COUNT(e) > :limit
            """)
    List<UUID> findArtistsExceedingRecentLimit(UUID userId, OffsetDateTime since, Collection<EventStatus> excluded, long limit);

    /**
     * Devuelve venue slugs donde el usuario supera el límite total de eventos.
     */
    @Query("""
                SELECT e.venueSlug
                FROM Event e
                WHERE e.createdByUserId = :userId
                  AND e.status NOT IN :excluded
                GROUP BY e.venueSlug
                HAVING COUNT(e) > :limit
            """)
    List<String> findVenuesExceedingTotalLimit(UUID userId, Collection<EventStatus> excluded, long limit);

    /**
     * Devuelve artist IDs donde el usuario supera el límite total de eventos.
     */
    @Query("""
                SELECT a.id
                FROM Event e
                JOIN e.artists a
                WHERE e.createdByUserId = :userId
                  AND e.status NOT IN :excluded
                GROUP BY a.id
                HAVING COUNT(e) > :limit
            """)
    List<UUID> findArtistsExceedingTotalLimit(UUID userId, Collection<EventStatus> excluded, long limit);
}
