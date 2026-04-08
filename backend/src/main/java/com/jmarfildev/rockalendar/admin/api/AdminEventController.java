package com.jmarfildev.rockalendar.admin.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.admin.api.dto.AdminEventListItemDto;
import com.jmarfildev.rockalendar.admin.api.dto.AdminStatusOverrideRequest;
import com.jmarfildev.rockalendar.admin.application.AdminEventCommandService;
import com.jmarfildev.rockalendar.admin.application.AdminEventQueryService;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 */
@RestController
@RequiredArgsConstructor
public class AdminEventController implements AdminEventApi {

    private final AdminEventCommandService commandService;
    private final AdminEventQueryService queryService;

    @Override
    public Page<AdminEventListItemDto> listEvents(List<EventStatus> statuses,
                                                   Optional<Short> provinceId,
                                                   Optional<OffsetDateTime> dateFrom,
                                                   Optional<OffsetDateTime> dateTo,
                                                   Optional<String> q,
                                                   Pageable pageable) {
        return queryService.listEvents(statuses, provinceId, dateFrom, dateTo, q, pageable);
    }

    @Override
    public EventPrivateDto edit(UUID eventId, SubmitEventRequest request, MultipartFile poster, boolean removePoster) {
        return commandService.edit(eventId, request, poster, removePoster);
    }

    @Override
    public EventPrivateDto overrideStatus(UUID eventId, AdminStatusOverrideRequest request) {
        return commandService.overrideStatus(eventId, request);
    }
}
