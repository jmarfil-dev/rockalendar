package com.jmarfildev.rockalendar.events.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.jmarfildev.rockalendar.common.annotations.ApiBadRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiConflict;
import com.jmarfildev.rockalendar.common.annotations.ApiForbidden;
import com.jmarfildev.rockalendar.common.annotations.ApiNotFound;
import com.jmarfildev.rockalendar.common.annotations.ApiUnauthorized;
import com.jmarfildev.rockalendar.events.api.doc.EventPrivatePageDoc;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;

/**
 * @author jmarfil
 *
 */
@Tag(name = "My Events", description = "Gestión de eventos del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public interface MeEventApi {

    @PostMapping("/api/events")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Proponer un nuevo evento",
            description = "Permite al usuario autenticado proponer un nuevo evento. El evento quedar PENDING_MODERATION antes de ser visible públicamente.")
    @ApiResponse(responseCode = "201", description = "Evento propuesto correctamente")
    @ApiUnauthorized
    @ApiBadRequest
    EventPrivateDto propose(@Parameter(description = "Datos del evento a proponer",
                                    required = true) @Valid @RequestBody SubmitEventRequest request);

    @GetMapping("/api/me/events")
    @Operation(summary = "Listar mis eventos",
            description = "Devuelve los eventos creados por el usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Listado de eventos del usuario",
            content = @Content(schema = @Schema(implementation = EventPrivatePageDoc.class)))
    @ApiUnauthorized
    Page<EventPrivateDto> listMine(@Parameter(description = "Paginación (page, size, sort)") @PageableDefault(size = 20) Pageable pageable);

    @PutMapping("/api/me/events/{eventId}")
    @Operation(summary = "Actualizar un evento propio",
            description = "Permite editar un evento del usuario en estados editables (DRAFT/NEEDS_CHANGES/APPROVED). "
                    + "Al actualizarlo, pasa a PENDING_MODERATION y se actualiza submittedAt.")
    @ApiResponse(responseCode = "200", description = "Evento actualizado correctamente")
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiBadRequest
    @ApiConflict
    EventPrivateDto update(@Parameter(description = "ID del evento", example = "cccccccc-0000-0000-0000-000000000001",
            required = true) @PathVariable UUID eventId,
                                  @Parameter(description = "Datos nuevos del evento",
                                          required = true) @Valid @RequestBody SubmitEventRequest request);
}
