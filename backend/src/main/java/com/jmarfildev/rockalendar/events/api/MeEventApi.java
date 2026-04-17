package com.jmarfildev.rockalendar.events.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto;
import com.jmarfildev.rockalendar.events.api.dto.ProposeEventResponse;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.events.application.MeEventTabEnum;

/**
 * @author jmarfil
 *
 */
@RequestMapping("/api/me/events")
@Tag(name = "Mis Eventos", description = "Gestión de eventos de usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public interface MeEventApi {

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @RequestBody(content = @Content(encoding = @Encoding(name = "event", contentType = "application/json")))
    @Operation(summary = "Proponer un nuevo evento",
               description = "Permite al usuario autenticado proponer un nuevo evento. El evento queda en PENDING_MODERATION antes de ser visible públicamente. "
                       + "El cartel es opcional; si se incluye se redimensiona a máximo 1200px y se convierte a JPEG.")
    @ApiResponse(responseCode = "201", description = "Evento propuesto correctamente")
    @ApiUnauthorized
    @ApiBadRequest
    ProposeEventResponse propose(@Parameter(description = "Datos del evento a proponer",
                                            required = true) @Valid @RequestPart("event") SubmitEventRequest request,
                                 @Parameter(description = "Cartel del evento (opcional)") @RequestPart(value = "poster",
                                                                                                       required = false) MultipartFile poster);

    @GetMapping
    @Operation(summary = "Listar mis eventos",
               description = """
                             Devuelve los eventos creados por el usuario autenticado.
                             Tiene 4 pestañas posibles:
                             - CHANGES: eventos en estado NEEDS_CHANGES, ordenación por pageable.
                             - PENDING: eventos en estado PENDING_MODERATION, ordenación por pageable.
                             - OTHERS: eventos en estados distintos a NEEDS_CHANGES y PENDING_MODERATION, ordenación por pageable.
                             - ALL (pestaña por defecto): todos los eventos, ordenación por defecto que ignora sort en pageable.

                             La ordenación por pageable permite direcciones asc y desc, y los campos title, date (fecha de submit),
                             province y city, y en OTHERS, además, status. Ignora cualquier valor distinto.
                             """)
    @ApiResponse(responseCode = "200",
                 description = "Listado de eventos del usuario",
                 content = @Content(schema = @Schema(implementation = EventPrivatePageDoc.class)))
    @ApiUnauthorized
    Page<EventPrivateListItemDto> listMine(@Parameter(description = "Nombre de la pestaña") @RequestParam(defaultValue = "ALL") MeEventTabEnum tab,
                                           @Parameter(description = "Paginación (page, size, sort)") @PageableDefault(size = 20) Pageable pageable);

    @GetMapping("/{eventId}")
    @Operation(summary = "Obtener detalle de un evento propio",
               description = "Devuelve el detalle completo de un evento del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Detalle del evento")
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    EventPrivateDto getMyEvent(@Parameter(description = "ID del evento",
                                          example = "cccccccc-0000-0000-0000-000000000001",
                                          required = true) @PathVariable UUID eventId);

    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequestBody(content = @Content(encoding = @Encoding(name = "event", contentType = "application/json")))
    @Operation(summary = "Actualizar un evento propio",
               description = "Permite editar un evento del usuario en estados editables (DRAFT/NEEDS_CHANGES/APPROVED). "
                       + "Al actualizarlo, pasa a PENDING_MODERATION y se actualiza submittedAt. "
                       + "Si se incluye un cartel nuevo, reemplaza al anterior. "
                       + "Si se omite y removePoster=false (por defecto), el cartel existente se conserva. "
                       + "Si se omite y removePoster=true, el cartel existente se elimina del servidor.")
    @ApiResponse(responseCode = "200", description = "Evento actualizado correctamente")
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiBadRequest
    @ApiConflict
    EventPrivateDto update(@Parameter(description = "ID del evento",
                                      example = "cccccccc-0000-0000-0000-000000000001",
                                      required = true) @PathVariable UUID eventId,
                           @Parameter(description = "Datos nuevos del evento",
                                      required = true) @Valid @RequestPart("event") SubmitEventRequest request,
                           @Parameter(description = "Cartel nuevo (opcional; si se omite se conserva el actual)") @RequestPart(value = "poster",
                                                                                                                               required = false) MultipartFile poster,
                           @Parameter(description = "Si es true y no se envía poster, elimina el cartel existente") @RequestParam(defaultValue = "false") boolean removePoster);

    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar (retirar) un evento propio",
               description = "Cambia el estado del evento a ERASED. Solo permitido en PENDING_MODERATION y NEEDS_CHANGES. "
                       + "Si el evento está APPROVED devuelve 409 indicando que hay que contactar con moderación.")
    @ApiResponse(responseCode = "204", description = "Evento eliminado correctamente")
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiBadRequest
    @ApiConflict
    void delete(@Parameter(description = "ID del evento",
                           example = "cccccccc-0000-0000-0000-000000000001",
                           required = true) @PathVariable UUID eventId);
}
