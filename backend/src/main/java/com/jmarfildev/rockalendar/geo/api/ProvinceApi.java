package com.jmarfildev.rockalendar.geo.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.jmarfildev.rockalendar.common.dto.ComboItemDto;

/**
 * @author jmarfil
 *
 */
@RequestMapping("/api/provinces")
@Tag(name = "Provinces", description = "Operaciones públicas con provincias")
public interface ProvinceApi {

    @GetMapping("/combo")
    @Operation(summary = "Lista todas las provincias para el combo",
               description = "Devuelve una lista de provincias.")
    @ApiResponse(responseCode = "200",
                 description = "Lista de provincias",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboItemDto.class))))
    List<ComboItemDto> listCombo();
}
