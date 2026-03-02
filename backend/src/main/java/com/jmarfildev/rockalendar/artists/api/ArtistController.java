package com.jmarfildev.rockalendar.artists.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.artists.application.ArtistQueryService;
import com.jmarfildev.rockalendar.common.dto.ComboItemDto;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
public class ArtistController implements ArtistApi {

    private final ArtistQueryService service;

    @Override
    public List<ComboItemDto> searchArtistsAutocomplete(String query) {
        return service.searchArtistsAutocomplete(query);
    }

    @Override
    public ArtistDto getById(UUID id) {
        return service.getById(id);
    }

}
