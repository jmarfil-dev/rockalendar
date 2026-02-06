package com.jmarfildev.rockalendar.artists.api;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.artists.api.dto.CreateArtistRequest;
import com.jmarfildev.rockalendar.artists.api.mapper.ArtistMapper;
import com.jmarfildev.rockalendar.artists.application.ArtistCommandService;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
public class ArtistModerationController implements ArtistModerationApi {

    private final ArtistCommandService artistService;
    private final ArtistMapper mapper;

    @Override
    public ArtistDto createArtist(CreateArtistRequest request) {
        return mapper.toDto(artistService.createArtist(request));
    }

}
