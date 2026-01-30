package com.jmarfildev.rockalendar.events.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

import com.jmarfildev.rockalendar.events.api.doc.EventPrivatePageDoc;
import com.jmarfildev.rockalendar.events.api.dto.ProposeEventRequest;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;

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
            description = "Permite al usuario autenticado proponer un nuevo evento. El evento quedará pendiente de moderación antes de ser visible públicamente.")
    @ApiResponse(responseCode = "201", description = "Evento propuesto correctamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    EventPrivateDto propose(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                            @Parameter(description = "Datos del evento a proponer",
                                    required = true) @Valid @RequestBody ProposeEventRequest request);

    @GetMapping("/api/me/events")
    @Operation(summary = "Listar mis eventos",
            description = "Devuelve los eventos creados por el usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Listado de eventos del usuario",
            content = @Content(schema = @Schema(implementation = EventPrivatePageDoc.class)))
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    Page<EventPrivateDto> listMine(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                   @Parameter(description = "Paginación (page, size, sort)") @PageableDefault(size = 20) Pageable pageable);
}
