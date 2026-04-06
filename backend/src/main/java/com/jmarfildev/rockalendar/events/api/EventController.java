package com.jmarfildev.rockalendar.events.api;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.events.api.dto.EventPublicDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicListItemDto;
import com.jmarfildev.rockalendar.events.application.EventQueryService;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
public class EventController implements EventApi {

    private final EventQueryService queryService;

    @Override
    public Page<EventPublicListItemDto> listHome(Pageable pageable) {
        return queryService.listHome(pageable);
    }

    @Override
    public Page<EventPublicListItemDto> searchPublic(Optional<String> query,
                                                     Optional<OffsetDateTime> dateFrom,
                                                     Optional<OffsetDateTime> dateTo,
                                                     Optional<Short> provinceId,
                                                     Optional<String> city,
                                                     Optional<UUID> artistId,
                                                     Pageable pageable) {
        return queryService.searchPublic(query, dateFrom, dateTo, provinceId, city, artistId, pageable);
    }

    @Override
    public EventPublicDto getPublicById(UUID id) {
        return queryService.getPublicById(id);
    }
}
