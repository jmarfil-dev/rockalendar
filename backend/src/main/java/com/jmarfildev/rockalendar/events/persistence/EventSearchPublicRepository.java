package com.jmarfildev.rockalendar.events.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.jmarfildev.rockalendar.events.api.dto.EventPublicSearchProjection;
import com.jmarfildev.rockalendar.events.domain.Event;

/**
 * @author jmarfil
 *
 */
public interface EventSearchPublicRepository extends Repository<Event, UUID> {

    @Query(
            value = """
                    SELECT
                      e.id                  AS id,
                      e.title               AS title,
                      e.description         AS description,
                      e.start_date_time     AS startDateTime,
                      e.end_date_time       AS endDateTime,
                      e.venue_name          AS venueName,
                      e.province_id         AS provinceId,
                      p.name                AS provinceName,
                      e.city_name           AS cityName,
                      COALESCE(art.artists, ARRAY[]::text[]) AS artists,
                      e.source_url          AS sourceUrl
                    FROM search_events(
                      :q, :minSim, :ftsW, :trgmW,
                      :dateFrom, :dateTo,
                      :provinceId, :citySlug, :artistSlug
                    ) s
                    JOIN events e ON e.id = s.event_id
                    JOIN provinces p ON p.id = e.province_id
                    LEFT JOIN LATERAL (
                      SELECT array_agg(a.name ORDER BY a.name) AS artists
                      FROM event_artists ea
                      JOIN artists a ON a.id = ea.artist_id
                      WHERE ea.event_id = e.id
                    ) art ON true
                    ORDER BY
                      CASE WHEN e.start_date_time >= now() THEN 0 ELSE 1 END ASC,
                      s.score DESC,
                      CASE WHEN e.start_date_time >= now() THEN e.start_date_time END ASC,
                      CASE WHEN e.start_date_time <  now() THEN e.start_date_time END DESC,
                      e.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM search_events(
                      :q, :minSim, :ftsW, :trgmW,
                      :dateFrom, :dateTo,
                      :provinceId, :citySlug, :artistSlug
                    )
                    """,
            nativeQuery = true)
    Page<EventPublicSearchProjection> searchPublic(@Param("q") String query,
                                                   @Param("minSim") double minSimilarity,
                                                   @Param("ftsW") double ftsWeight,
                                                   @Param("trgmW") double trgmWeight,
                                                   @Param("dateFrom") OffsetDateTime dateFrom,
                                                   @Param("dateTo") OffsetDateTime dateTo,
                                                   @Param("provinceId") UUID provinceId,
                                                   @Param("citySlug") String citySlug,
                                                   @Param("artistSlug") String artistSlug,
                                                   Pageable pageable);

    @Query(
            value = """
                    SELECT
                      e.id                  AS id,
                      e.title               AS title,
                      e.description         AS description,
                      e.start_date_time     AS startDateTime,
                      e.end_date_time       AS endDateTime,
                      e.venue_name          AS venueName,
                      e.province_id         AS provinceId,
                      p.name                AS provinceName,
                      e.city_name           AS cityName,
                      COALESCE(art.artists, ARRAY[]::text[]) AS artists,
                      e.source_url          AS sourceUrl
                      FROM search_events_or(
                        :q, :minSim, :ftsW, :trgmW,
                        :dateFrom, :dateTo,
                        :provinceId, :citySlug, :artistSlug
                      ) s
                      JOIN events e ON e.id = s.event_id
                      JOIN provinces p ON p.id = e.province_id
                      LEFT JOIN LATERAL (
                        SELECT array_agg(a.name ORDER BY a.name) AS artists
                        FROM event_artists ea
                        JOIN artists a ON a.id = ea.artist_id
                        WHERE ea.event_id = e.id
                      ) art ON true
                      ORDER BY
                        CASE WHEN e.start_date_time >= now() THEN 0 ELSE 1 END ASC,
                        s.score DESC,
                        CASE WHEN e.start_date_time >= now() THEN e.start_date_time END ASC NULLS LAST,
                        CASE WHEN e.start_date_time <  now() THEN e.start_date_time END DESC NULLS LAST,
                        e.id ASC
                    """,
            countQuery = """
                      SELECT COUNT(*)
                      FROM search_events_or(
                        :q, :minSim, :ftsW, :trgmW,
                        :dateFrom, :dateTo,
                        :provinceId, :citySlug, :artistSlug
                      )
                    """,
            nativeQuery = true)
    Page<EventPublicSearchProjection> searchPublicOrFallback(@Param("q") String query,
                                                             @Param("minSim") double minSimilarity,
                                                             @Param("ftsW") double ftsWeight,
                                                             @Param("trgmW") double trgmWeight,
                                                             @Param("dateFrom") OffsetDateTime dateFrom,
                                                             @Param("dateTo") OffsetDateTime dateTo,
                                                             @Param("provinceId") UUID provinceId,
                                                             @Param("citySlug") String citySlug,
                                                             @Param("artistSlug") String artistSlug,
                                                             Pageable pageable);
}
