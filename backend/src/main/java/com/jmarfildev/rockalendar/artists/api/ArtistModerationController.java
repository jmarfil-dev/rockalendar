package com.jmarfildev.rockalendar.artists.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.artists.api.dto.CreateArtistRequest;
import com.jmarfildev.rockalendar.artists.api.mapper.ArtistMapper;
import com.jmarfildev.rockalendar.artists.application.ArtistCommandService;
import com.jmarfildev.rockalendar.artists.application.ArtistQueryService;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
public class ArtistModerationController implements ArtistModerationApi {

    private final ArtistCommandService artistService;
    private final ArtistQueryService artistQueryService;
    private final ArtistMapper mapper;

    @Override
    public Page<ArtistDto> getOrphanArtists(String query, Pageable pageable) {
        return artistQueryService.findOrphans(query, pageable);
    }

    @Override
    public ArtistDto createArtist(CreateArtistRequest request) {
        return mapper.toDto(artistService.createArtist(request));
    }

    @Override
    public ArtistDto renameArtist(UUID id, CreateArtistRequest request) {
        return mapper.toDto(artistService.renameArtist(id, request));
    }

    @Override
    public void deleteArtist(UUID id) {
        artistService.deleteArtist(id);
    }

}
