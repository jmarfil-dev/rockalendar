package com.jmarfildev.rockalendar.agenda.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jmarfildev.rockalendar.agenda.api.dto.AgendaItemDto;
import com.jmarfildev.rockalendar.agenda.domain.UserEvent;
import com.jmarfildev.rockalendar.agenda.domain.UserEventId;

/**
 * @author jmarfil
 */
public interface UserEventRepository extends JpaRepository<UserEvent, UserEventId> {

    @Query("""
                SELECT new com.jmarfildev.rockalendar.agenda.api.dto.AgendaItemDto(
                    e.id, e.title, e.startDateTime, e.startTimeUnknown, e.endDate, e.venueName, e.cityName, p.name, ue.status, ue.createdAt
                )
                FROM UserEvent ue
                JOIN ue.event e
                JOIN e.province p
                WHERE ue.id.userId = :userId
                  AND e.status = com.jmarfildev.rockalendar.events.domain.EventStatus.APPROVED
                  AND (
                      (e.endDate IS NOT NULL AND e.endDate >= CURRENT_DATE)
                      OR (e.endDate IS NULL AND e.startDateTime >= CURRENT_TIMESTAMP)
                  )
                ORDER BY e.startDateTime ASC, e.title ASC
            """)
    List<AgendaItemDto> findAgendaByUserId(UUID userId);
}
