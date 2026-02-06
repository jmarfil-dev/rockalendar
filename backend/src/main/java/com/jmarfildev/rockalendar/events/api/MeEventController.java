package com.jmarfildev.rockalendar.events.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.ProposeEventRequest;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapper;
import com.jmarfildev.rockalendar.events.application.EventCommandService;
import com.jmarfildev.rockalendar.events.application.EventQueryService;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
public class MeEventController implements MeEventApi {

    private final EventCommandService commandService;
    private final EventQueryService queryService;
    private final EventMapper mapper;

    @Override
    public EventPrivateDto propose(ProposeEventRequest request) {
        return mapper.toPrivateDto(commandService.propose(request));
    }

    @Override
    public Page<EventPrivateDto> listMine(Pageable pageable) {
        return queryService.listMine(pageable);
    }
}
