package com.jmarfildev.rockalendar.artists.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
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
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = ArtistDto.class))))
    List<ArtistDto> searchArtistsAutocomplete(@Parameter(description = "Texto libre de búsqueda por nombre de artista, 2-50 caracteres",
                                                         example = "maiden",
                                                         required = true) @NotBlank @Size(min = 2, max = 50) @RequestParam String query);
}
