package com.jmarfildev.rockalendar.artists.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

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
import com.jmarfildev.rockalendar.common.annotations.ApiNotFound;
import com.jmarfildev.rockalendar.common.annotations.ApiUnauthorized;

/**
 * @author jmarfil
 *
 */
@RequestMapping("/api/moderation/artists")
@Tag(name = "Admin Artists", description = "Operaciones privadas con artistas")
@SecurityRequirement(name = "bearerAuth")
public interface ArtistModerationApi {

    @GetMapping
    @Operation(summary = "Listar artistas huérfanos",
            description = "Devuelve artistas que no están vinculados a ningún concierto. Solo MODERATOR o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista paginada de artistas sin concierto")
    @ApiUnauthorized
    @ApiForbidden
    Page<ArtistDto> getOrphanArtists(
            @Parameter(description = "Filtro por nombre (opcional)") @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "name") Pageable pageable);

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
    ArtistDto createArtist(@Parameter(description = "Datos del artista", required = true) @Valid @RequestBody CreateArtistRequest request);

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar artista",
            description = "Elimina un artista. No se puede eliminar si tiene conciertos asociados.")
    @ApiResponse(responseCode = "204", description = "Artista eliminado correctamente")
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiConflict
    void deleteArtist(@Parameter(description = "ID del artista", required = true) @PathVariable UUID id);
}
