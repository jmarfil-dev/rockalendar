package com.jmarfildev.rockalendar.artists.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.common.annotations.ApiNotFound;
import com.jmarfildev.rockalendar.common.dto.ComboItemDto;

/**
 * @author jmarfil
 *
 */
@RequestMapping("/api/artists")
@Tag(name = "Artists", description = "Operaciones públicas con artistas")
public interface ArtistApi {

    @GetMapping
    @Operation(summary = "Busca artistas para el autocompletado",
               description = "Devuelve una lista de artistas, de entre 0 y 10 resultados, que coinciden con la query.")
    @ApiResponse(responseCode = "200",
                 description = "Lista de artistas",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboItemDto.class))))
    List<ComboItemDto> searchArtistsAutocomplete(@Parameter(description = "Texto libre de búsqueda por nombre de artista, 2-50 caracteres",
                                                            example = "maiden",
                                                            required = true) @NotBlank @Size(min = 2, max = 50) @RequestParam String query);

    @GetMapping("/{id}")
    @Operation(summary = "Obtener artista por ID")
    @ApiResponse(responseCode = "200",
                 description = "Artista encontrado",
                 content = @Content(schema = @Schema(implementation = ArtistDto.class)))
    @ApiNotFound
    ArtistDto getById(@Parameter(description = "ID del artista",
                                      example = "cccccccc-0000-0000-0000-000000000001",
                                      required = true) @PathVariable UUID id);
}
