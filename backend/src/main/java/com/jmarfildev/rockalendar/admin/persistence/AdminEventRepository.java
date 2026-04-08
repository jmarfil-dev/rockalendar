package com.jmarfildev.rockalendar.admin.persistence;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.jmarfildev.rockalendar.admin.api.dto.AdminEventListItemDto;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 */
public interface AdminEventRepository extends Repository<Event, UUID> {

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
}
