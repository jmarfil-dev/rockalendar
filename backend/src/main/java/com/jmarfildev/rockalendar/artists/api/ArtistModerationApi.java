package com.jmarfildev.rockalendar.artists.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.artists.api.dto.CreateArtistRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiBadRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiConflict;
import com.jmarfildev.rockalendar.common.annotations.ApiForbidden;
import com.jmarfildev.rockalendar.common.annotations.ApiUnauthorized;

/**
 * @author jmarfil
 *
 */
@RequestMapping("/api/moderation/artists")
@Tag(name = "Admin Artists", description = "Operaciones privadas con artistas")
@SecurityRequirement(name = "bearerAuth")
public interface ArtistModerationApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear artista",
            description = "Crea un artista nuevo. Solo usuarios con rol MODERATOR o ADMIN pueden crear artistas de forma explícita.")
    @ApiResponse(responseCode = "201", description = "Artista creado correctamente",
            content = @Content(schema = @Schema(implementation = ArtistDto.class)))
    @ApiUnauthorized
    @ApiForbidden
    @ApiBadRequest
    @ApiConflict
    ArtistDto createArtist(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                           @Parameter(description = "Datos del artista", required = true) @Valid @RequestBody CreateArtistRequest request);
}
