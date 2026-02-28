package com.jmarfildev.rockalendar.events.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.events.application.EventCommandService;
import com.jmarfildev.rockalendar.events.application.EventQueryService;
import com.jmarfildev.rockalendar.events.application.MeEventTabEnum;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
public class MeEventController implements MeEventApi {

    private final EventCommandService commandService;
    private final EventQueryService queryService;

    @Override
    public EventPrivateDto propose(SubmitEventRequest request) {
        return commandService.propose(request);
    }

    @Override
    public Page<EventPrivateListItemDto> listMine(MeEventTabEnum tab, Pageable pageable) {
        return queryService.listMine(tab, pageable);
    }

    @Override
    public EventPrivateDto update(UUID eventId, SubmitEventRequest request) {
        return commandService.update(eventId, request);
    }

    @Override
    public void delete(UUID eventId) {
        commandService.delete(eventId);
    }
}
