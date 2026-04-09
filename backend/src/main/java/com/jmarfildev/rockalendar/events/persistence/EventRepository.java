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

import com.jmarfildev.rockalendar.admin.api.dto.AdminEventListItemDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicListItemDto;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApprovedListItemDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedListItemDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingListItemDto;

/**
 * @author jmarfil
 *
 */
public interface EventRepository extends JpaRepository<Event, UUID> {

    @EntityGraph(attributePaths = { "province", "artists" })
    Optional<Event> findByIdAndStatus(UUID id, EventStatus status);

    // Sobreescribe findById de JpaRepository para cargar province y artists con JOIN FETCH
    @Override
    @EntityGraph(attributePaths = { "province", "artists" })
    Optional<Event> findById(UUID id);

    @Query("""
               SELECT new com.jmarfildev.rockalendar.events.api.dto.EventPublicListItemDto(
                   e.id,
                   e.title,
                   e.startDateTime,
                   e.startTimeUnknown,
                   e.endDate,
                   p.name,
                   e.cityName,
                   e.posterUrl
               )
               FROM Event e
               JOIN e.province p
               WHERE e.status = com.jmarfildev.rockalendar.events.domain.EventStatus.APPROVED
                   AND (
                       (e.endDate IS NOT NULL AND e.endDate >= CURRENT_DATE)
                           OR (e.endDate IS NULL AND e.startDateTime >= CURRENT_TIMESTAMP)
                   )
           """)
    Page<EventPublicListItemDto> findHome(Pageable pageable);

    @Query("""
               SELECT new com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto(
                   e.id, e.title, e.startDateTime, e.startTimeUnknown, p.name, e.cityName, e.status, e.moderationMessage, e.submittedAt
               )
               FROM Event e
               JOIN e.province p
               WHERE e.createdByUserId = :userId
                 AND e.status IN :statuses
           """)
    Page<EventPrivateListItemDto> listMineByStatuses(UUID userId, Collection<EventStatus> statuses, Pageable pageable);

    @Query("""
               SELECT new com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto(
                   e.id, e.title, e.startDateTime, e.startTimeUnknown, p.name, e.cityName, e.status, e.moderationMessage, e.submittedAt
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
                   e.id, e.title, e.startDateTime, e.startTimeUnknown, p.name, e.cityName, e.status, e.moderationMessage, e.submittedAt
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
                     e.start_date_time    AS startDateTime,
                     e.start_time_unknown AS startTimeUnknown,
                     e.end_date           AS endDate,
                     p.name               AS provinceName,
                     e.city_name          AS cityName,
                     e.poster_url         AS posterUrl,
                     s.score              AS score
                   FROM search_public_events(
                     :q, :minSim, :ftsW, :trgmW,
                     :dateFrom, :dateTo,
                     :provinceId, :citySlug, :artistId
                   ) s
                   JOIN events e ON e.id = s.event_id
                   JOIN provinces p ON p.ine_code = e.province_id
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
    @SuppressWarnings("java:S107") // Los parámetros mapean 1:1 con la firma de la función PostgreSQL
    Page<EventPublicSearchProjection> searchPublicEvents(@Param("q") String q,
                                                         @Param("minSim") double minSim,
                                                         @Param("ftsW") double ftsW,
                                                         @Param("trgmW") double trgmW,
                                                         @Param("dateFrom") OffsetDateTime dateFrom,
                                                         @Param("dateTo") OffsetDateTime dateTo,
                                                         @Param("provinceId") Short provinceId,
                                                         @Param("citySlug") String citySlug,
                                                         @Param("artistId") UUID artistId,
                                                         @Param("sortKey") String sortKey,
                                                         @Param("sortDir") String sortDir,
                                                         Pageable pageable);

    @Query(value = """
                   SELECT
                     e.id              AS id,
                     e.title           AS title,
                     e.start_date_time    AS startDateTime,
                     e.start_time_unknown AS startTimeUnknown,
                     e.end_date           AS endDate,
                     p.name               AS provinceName,
                     e.city_name          AS cityName,
                     e.poster_url         AS posterUrl,
                     s.score              AS score
                   FROM search_public_events_fallback(
                     :q, :minSim, :ftsW, :trgmW,
                     :dateFrom, :dateTo,
                     :provinceId, :citySlug, :artistId
                   ) s
                   JOIN events e ON e.id = s.event_id
                   JOIN provinces p ON p.ine_code = e.province_id
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
    @SuppressWarnings("java:S107") // Los parámetros mapean 1:1 con la firma de la función PostgreSQL
    Page<EventPublicSearchProjection> searchPublicEventsFallback(@Param("q") String q,
                                                                 @Param("minSim") double minSim,
                                                                 @Param("ftsW") double ftsW,
                                                                 @Param("trgmW") double trgmW,
                                                                 @Param("dateFrom") OffsetDateTime dateFrom,
                                                                 @Param("dateTo") OffsetDateTime dateTo,
                                                                 @Param("provinceId") Short provinceId,
                                                                 @Param("citySlug") String citySlug,
                                                                 @Param("artistId") UUID artistId,
                                                                 @Param("sortKey") String sortKey,
                                                                 @Param("sortDir") String sortDir,
                                                                 Pageable pageable);

    // --- Queries de moderación ---

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

    // --- Queries de administración ---

    @Query("""
           SELECT new com.jmarfildev.rockalendar.admin.api.dto.AdminEventListItemDto(
               e.id, e.title, e.startDateTime, e.startTimeUnknown, p.name, e.status
           )
           FROM Event e
           JOIN e.province p
           WHERE e.status IN :statuses
             AND (:provinceId IS NULL OR e.province.ineCode = :provinceId)
             AND e.startDateTime >= COALESCE(:dateFrom, e.startDateTime)
             AND e.startDateTime <= COALESCE(:dateTo,   e.startDateTime)
             AND (:titleLike  IS NULL OR LOWER(e.title) LIKE :titleLike)
             AND (
                 (e.endDate IS NOT NULL AND e.endDate >= CURRENT_DATE)
                 OR (e.endDate IS NULL  AND e.startDateTime >= CURRENT_TIMESTAMP)
             )
           """)
    Page<AdminEventListItemDto> findAdminEvents(@Param("statuses") Collection<EventStatus> statuses,
                                                @Param("provinceId") Short provinceId,
                                                @Param("dateFrom") OffsetDateTime dateFrom,
                                                @Param("dateTo") OffsetDateTime dateTo,
                                                @Param("titleLike") String titleLike,
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
