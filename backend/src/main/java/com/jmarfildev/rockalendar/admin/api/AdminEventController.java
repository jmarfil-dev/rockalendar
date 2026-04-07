package com.jmarfildev.rockalendar.admin.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.web.multipart.MultipartFile;

import com.jmarfildev.rockalendar.admin.api.dto.AdminStatusOverrideRequest;
import com.jmarfildev.rockalendar.admin.application.AdminEventCommandService;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;

/**
 * @author jmarfil
 */
@RestController
@RequiredArgsConstructor
public class AdminEventController implements AdminEventApi {

    private final AdminEventCommandService commandService;

    @Override
    public EventPrivateDto edit(UUID eventId, SubmitEventRequest request, MultipartFile poster, boolean removePoster) {
        return commandService.edit(eventId, request, poster, removePoster);
    }

    @Override
    public EventPrivateDto overrideStatus(UUID eventId, AdminStatusOverrideRequest request) {
        return commandService.overrideStatus(eventId, request);
    }
}
