package com.jmarfildev.rockalendar.events.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.events.api.dto.ProposeEventRequest;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
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
    public EventPrivateDto propose(Jwt jwt, ProposeEventRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return mapper.toPrivateDto(commandService.proposeEvent(request, userId));
    }

    @Override
    public Page<EventPrivateDto> listMine(Jwt jwt, Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return queryService.listMine(userId, pageable);
    }
}
