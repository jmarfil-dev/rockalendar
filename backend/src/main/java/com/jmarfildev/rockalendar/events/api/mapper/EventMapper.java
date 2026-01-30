package com.jmarfildev.rockalendar.events.api.mapper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.jmarfildev.rockalendar.artists.domain.Artist;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicSearchProjection;
import com.jmarfildev.rockalendar.events.domain.Event;

/**
 * @author jmarfil
 *
 */
@Mapper(componentModel = "spring")
public interface EventMapper {
    default List<String> toList(String[] arr) {
        return arr == null ? List.of() : Arrays.asList(arr);
    }

    default List<String> mapArtists(Event event) {
        return event.getArtists()
                .stream()
                .map(Artist::getName)
                .toList();
    }

    default UUID mapProvinceId(Event event) {
        return event.getProvince() == null ? null : event.getProvince().getId();
    }

    default String mapProvinceName(Event event) {
        return event.getProvince() == null ? null : event.getProvince().getName();
    }

    @Mapping(target = "artists", expression = "java(toList(projection.getArtists()))")
    @Mapping(target = "startDateTime",
            expression = "java(projection.getStartDateTime() == null ? null : projection.getStartDateTime().atOffset(java.time.ZoneOffset.UTC))")
    @Mapping(target = "endDateTime",
            expression = "java(projection.getEndDateTime() == null ? null : projection.getEndDateTime().atOffset(java.time.ZoneOffset.UTC))")
    EventPublicDto toPublicDto(EventPublicSearchProjection projection);

    @Mapping(target = "artists", expression = "java(mapArtists(event))")
    @Mapping(target = "provinceId", expression = "java(mapProvinceId(event))")
    @Mapping(target = "provinceName", expression = "java(mapProvinceName(event))")
    EventPublicDto toPublicDto(Event event);

    @Mapping(target = "artists", expression = "java(mapArtists(event))")
    @Mapping(target = "provinceId", expression = "java(mapProvinceId(event))")
    @Mapping(target = "provinceName", expression = "java(mapProvinceName(event))")
    EventPrivateDto toPrivateDto(Event event);
}
