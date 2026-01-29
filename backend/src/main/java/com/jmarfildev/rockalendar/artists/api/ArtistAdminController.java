package com.jmarfildev.rockalendar.artists.api;

import java.util.UUID;

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
public class ArtistAdminController implements ArtistAdminApi {

    private final ArtistCommandService artistService;
    private final ArtistMapper mapper;

    @Override
    public ArtistDto createArtist(CreateArtistRequest request) {
        // TODO: userId sacado de jwt
        UUID userId = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000003"); // usuario mock en dev
        return mapper.toDto(artistService.createArtist(request, userId));
    }

}
