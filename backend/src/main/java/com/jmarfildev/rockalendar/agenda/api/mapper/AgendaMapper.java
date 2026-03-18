package com.jmarfildev.rockalendar.agenda.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.jmarfildev.rockalendar.agenda.api.dto.AgendaItemDto;
import com.jmarfildev.rockalendar.agenda.domain.UserEvent;
import com.jmarfildev.rockalendar.events.domain.Event;

/**
 * @author jmarfil
 */
@Mapper(componentModel = "spring")
public interface AgendaMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "provinceName", source = "event.province.name")
    @Mapping(target = "status", source = "userEvent.status")
    @Mapping(target = "createdAt", source = "userEvent.createdAt")
    AgendaItemDto toDto(Event event, UserEvent userEvent);
}
