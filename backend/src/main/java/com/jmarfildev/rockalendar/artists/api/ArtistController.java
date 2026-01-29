package com.jmarfildev.rockalendar.artists.api;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.artists.application.ArtistQueryService;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
public class ArtistController implements ArtistApi {

    private final ArtistQueryService artistService;

    @Override
    public List<ArtistDto> searchArtistsAutocomplete(String query) {
        return artistService.searchArtistsAutocomplete(query);
    }

}
