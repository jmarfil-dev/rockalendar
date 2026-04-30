package com.jmarfildev.rockalendar.agenda.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.jmarfildev.rockalendar.agenda.api.dto.AgendaItemDto;
import com.jmarfildev.rockalendar.agenda.api.dto.SetInteractionRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiBadRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiConflict;
import com.jmarfildev.rockalendar.common.annotations.ApiNotFound;
import com.jmarfildev.rockalendar.common.annotations.ApiUnauthorized;

/**
 * @author jmarfil
 */
@RequestMapping("/api/me/agenda")
@Tag(name = "Agenda", description = "Gestión de la agenda personal del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public interface AgendaApi {

    @GetMapping
    @Operation(summary = "Obtener mi agenda",
               description = "Devuelve los eventos marcados como INTERESTED o GOING por el usuario autenticado. "
                       + "Solo incluye eventos en estado APPROVED, ordenados por fecha ascendente.")
    @ApiResponse(responseCode = "200",
                 description = "Agenda del usuario",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = AgendaItemDto.class))))
    @ApiUnauthorized
    List<AgendaItemDto> getAgenda();

    @PutMapping("/{eventId}")
    @Operation(summary = "Añadir o actualizar interacción con un evento",
               description = "Marca un evento como INTERESTED o GOING. Si ya existe una interacción, la actualiza. "
                       + "Solo se permite con eventos en estado APPROVED.")
    @ApiResponse(responseCode = "200",
                 description = "Interacción guardada correctamente",
                 content = @Content(schema = @Schema(implementation = AgendaItemDto.class)))
    @ApiUnauthorized
    @ApiNotFound
    @ApiBadRequest
    @ApiConflict
    AgendaItemDto upsert(@Parameter(description = "ID del evento",
                                    example = "cccccccc-0000-0000-0000-000000000001",
                                    required = true) @PathVariable UUID eventId,
                         @Parameter(description = "Tipo de interacción",
                                    required = true) @Valid @RequestBody SetInteractionRequest request);

    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar interacción con un evento",
               description = "Desmarca el evento de la agenda del usuario. Si no existía interacción, no hace nada (idempotente).")
    @ApiResponse(responseCode = "204", description = "Interacción eliminada correctamente")
    @ApiUnauthorized
    void remove(@Parameter(description = "ID del evento",
                           example = "cccccccc-0000-0000-0000-000000000001",
                           required = true) @PathVariable UUID eventId);
}
