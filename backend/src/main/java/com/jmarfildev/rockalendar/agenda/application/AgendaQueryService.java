package com.jmarfildev.rockalendar.agenda.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.agenda.api.dto.AgendaItemDto;
import com.jmarfildev.rockalendar.agenda.persistence.UserEventRepository;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;

/**
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgendaQueryService {

    private final UserEventRepository userEventRepository;
    private final CurrentUser currentUser;

    /**
     * Devuelve la agenda del usuario autenticado: eventos APPROVED marcados como
     * INTERESTED o GOING, ordenados por fecha ascendente.
     *
     * @return lista de ítems de agenda
     */
    public List<AgendaItemDto> getAgenda() {
        return userEventRepository.findAgendaByUserId(currentUser.userId());
    }
}
