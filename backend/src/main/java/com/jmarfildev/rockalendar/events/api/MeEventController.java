package com.jmarfildev.rockalendar.events.api;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.web.multipart.MultipartFile;

import com.jmarfildev.rockalendar.common.dto.PageResponse;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto;
import com.jmarfildev.rockalendar.events.api.dto.ProposeEventResponse;
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
    public ProposeEventResponse propose(SubmitEventRequest request, MultipartFile poster) {
        return commandService.propose(request, poster);
    }

    @Override
    public EventPrivateDto getMyEvent(UUID eventId) {
        return queryService.getMine(eventId);
    }

    @Override
    public PageResponse<EventPrivateListItemDto> listMine(MeEventTabEnum tab, Pageable pageable) {
        return PageResponse.of(queryService.listMine(tab, pageable));
    }

    @Override
    public EventPrivateDto update(UUID eventId, SubmitEventRequest request, MultipartFile poster, boolean removePoster) {
        return commandService.update(eventId, request, poster, removePoster);
    }

    @Override
    public void delete(UUID eventId) {
        commandService.delete(eventId);
    }
}
