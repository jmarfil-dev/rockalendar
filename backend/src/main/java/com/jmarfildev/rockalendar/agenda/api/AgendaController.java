package com.jmarfildev.rockalendar.agenda.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.agenda.api.dto.AgendaItemDto;
import com.jmarfildev.rockalendar.agenda.api.dto.SetInteractionRequest;
import com.jmarfildev.rockalendar.agenda.application.AgendaCommandService;
import com.jmarfildev.rockalendar.agenda.application.AgendaQueryService;

/**
 * @author jmarfil
 */
@RestController
@RequiredArgsConstructor
public class AgendaController implements AgendaApi {

    private final AgendaCommandService commandService;
    private final AgendaQueryService queryService;

    @Override
    public List<AgendaItemDto> getAgenda() {
        return queryService.getAgenda();
    }

    @Override
    public AgendaItemDto upsert(UUID eventId, SetInteractionRequest request) {
        return commandService.upsert(eventId, request.status());
    }

    @Override
    public void remove(UUID eventId) {
        commandService.remove(eventId);
    }
}
