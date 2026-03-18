package com.jmarfildev.rockalendar.agenda.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.agenda.api.dto.AgendaItemDto;
import com.jmarfildev.rockalendar.agenda.domain.InteractionStatus;
import com.jmarfildev.rockalendar.agenda.domain.UserEvent;
import com.jmarfildev.rockalendar.agenda.domain.UserEventId;
import com.jmarfildev.rockalendar.agenda.persistence.UserEventRepository;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;

/**
 * @author jmarfil
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgendaCommandService {

    private final UserEventRepository userEventRepository;
    private final EventRepository eventRepository;
    private final CurrentUser currentUser;

    /**
     * Crea o actualiza la interacción del usuario con un evento (INTERESTED / GOING).
     * Solo permite interaccionar con eventos en estado APPROVED.
     *
     * @param eventId ID del evento
     * @param status  tipo de interacción
     * @return ítem de agenda resultante
     */
    @Transactional
    public AgendaItemDto upsert(UUID eventId, InteractionStatus status) {
        UUID userId = currentUser.userId();

        Event event = eventRepository.findById(eventId)
                                     .orElseThrow(() -> new NotFoundException(ErrorConstants.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.APPROVED) {
            throw new ConflictException(ErrorConstants.AGENDA_EVENT_NOT_AVAILABLE, ErrorConstants.TYPE_409_EVENT_STATE);
        }

        UserEventId id = new UserEventId(userId, eventId);
        UserEvent userEvent = userEventRepository.findById(id)
                                                 .map(existing -> {
                                                     existing.setStatus(status);
                                                     return existing;
                                                 })
                                                 .orElse(UserEvent.builder().id(id).status(status).build());

        UserEvent saved = userEventRepository.save(userEvent);
        log.info("agenda upsert userId={} eventId={} status={}", userId, eventId, status);

        return new AgendaItemDto(
                event.getId(),
                event.getTitle(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getVenueName(),
                event.getCityName(),
                event.getProvince().getName(),
                saved.getStatus(),
                saved.getCreatedAt());
    }

    /**
     * Elimina la interacción del usuario con un evento (desmarca).
     * Es idempotente: si no existía, no hace nada.
     *
     * @param eventId ID del evento
     */
    @Transactional
    public void remove(UUID eventId) {
        UUID userId = currentUser.userId();
        UserEventId id = new UserEventId(userId, eventId);
        userEventRepository.deleteById(id);
        log.info("agenda remove userId={} eventId={}", userId, eventId);
    }
}
