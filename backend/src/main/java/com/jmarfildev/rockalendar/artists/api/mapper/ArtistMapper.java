package com.jmarfildev.rockalendar.artists.api.mapper;

import org.mapstruct.Mapper;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.artists.domain.Artist;

/**
 * @author jmarfil
 *
 */
@Mapper(componentModel = "spring")
public interface ArtistMapper {

    ArtistDto toDto(Artist artist);
}
