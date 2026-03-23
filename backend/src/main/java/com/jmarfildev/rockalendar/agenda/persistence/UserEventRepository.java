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
                    e.id, e.title, e.startDateTime, e.endDateTime, e.venueName, e.cityName, p.name, ue.status, ue.createdAt
                )
                FROM UserEvent ue
                JOIN ue.event e
                JOIN e.province p
                WHERE ue.id.userId = :userId
                  AND e.status = com.jmarfildev.rockalendar.events.domain.EventStatus.APPROVED
                  AND (
                      (e.endDateTime IS NOT NULL AND e.endDateTime >= CURRENT_TIMESTAMP)
                      OR (e.endDateTime IS NULL AND e.startDateTime >= CURRENT_TIMESTAMP)
                  )
                ORDER BY e.startDateTime ASC, e.title ASC
            """)
    List<AgendaItemDto> findAgendaByUserId(UUID userId);
}
