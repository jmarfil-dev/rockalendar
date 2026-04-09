package com.jmarfildev.rockalendar.moderation.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

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
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.moderation.api.doc.ModerationApprovedPageDoc;
import com.jmarfildev.rockalendar.moderation.api.doc.ModerationArchivedPageDoc;
import com.jmarfildev.rockalendar.moderation.api.doc.ModerationPendingPageDoc;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApproveRequest;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApprovedListItemDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchiveRequest;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedListItemDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationEventDetailDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingListItemDto;

/**
 * @author jmarfil
 *
 */
@RequestMapping("/api/moderation/events")
@Tag(name = "Moderation Events", description = "Operaciones de moderación de eventos")
@SecurityRequirement(name = "bearerAuth")
public interface ModerationEventApi {

    /*
     * Query endpoints
     */

    @GetMapping("/{eventId}")
    @Operation(summary = "Obtener detalle de un evento para moderación",
               description = "Devuelve el detalle completo de cualquier evento, independientemente de su estado.")
    @ApiResponse(responseCode = "200", description = "Detalle del evento")
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    ModerationEventDetailDto getForModeration(@Parameter(description = "ID del evento",
                                                          example = "cccccccc-0000-0000-0000-000000000001",
                                                          required = true) @PathVariable UUID eventId);

    @GetMapping("/pending")
    @Operation(summary = "Listar eventos pendientes de moderación",
               description = """
                             Devuelve eventos en estado PENDING_MODERATION. Listado ligero para revisión.

                             La ordenación por pageable permite direcciones asc y desc, y los campos title y submitted (fecha de submit).
                             Ignora cualquier valor distinto.
                             """)
    @ApiResponse(responseCode = "200",
                 description = "Listado paginado de eventos pendientes",
                 content = @Content(schema = @Schema(implementation = ModerationPendingPageDoc.class)))
    @ApiUnauthorized
    @ApiForbidden
    Page<ModerationPendingListItemDto> listPending(@PageableDefault(size = 20) Pageable pageable);

    @GetMapping("/approved")
    @Operation(summary = "Listar eventos aprobados",
               description = """
                             Devuelve eventos en estado APPROVED. Listado ligero para revisión y posible re-moderación.

                             La ordenación por pageable permite direcciones asc y desc, y los campos title y approved (fecha de aprobación).
                             Ignora cualquier valor distinto.
                             """)
    @ApiResponse(responseCode = "200",
                 description = "Listado paginado de eventos aprobados",
                 content = @Content(schema = @Schema(implementation = ModerationApprovedPageDoc.class)))
    @ApiUnauthorized
    @ApiForbidden
    Page<ModerationApprovedListItemDto> listApproved(@PageableDefault(size = 20) Pageable pageable);

    @GetMapping("/archived")
    @Operation(summary = "Listar eventos archivados de moderación",
               description = """
                             Devuelve eventos en estado REJECTED, HIDDEN o CANCELED. Incluye estado y último mensaje de moderación.

                             La ordenación por pageable permite direcciones asc y desc, y los campos title, status,
                             y moderated (fecha de moderación). Ignora cualquier valor distinto.
                             """)
    @ApiResponse(responseCode = "200",
                 description = "Listado paginado de eventos archivados",
                 content = @Content(schema = @Schema(implementation = ModerationArchivedPageDoc.class)))
    @ApiUnauthorized
    @ApiForbidden
    Page<ModerationArchivedListItemDto> listArchived(@PageableDefault(size = 20) Pageable pageable);

    /*
     * Command endpoints
     */

    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Editar datos de un evento pendiente",
               description = "Permite al moderador corregir los datos de un evento en PENDING_MODERATION sin cambiar su estado. "
                       + "Registra una acción MODERATOR_EDITED en el historial de moderación.")
    @ApiResponse(responseCode = "200", description = "Evento actualizado correctamente")
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiBadRequest
    @ApiConflict
    EventPrivateDto edit(@Parameter(description = "ID del evento",
                                    example = "cccccccc-0000-0000-0000-000000000001",
                                    required = true) @PathVariable UUID eventId,
                         @Parameter(description = "Datos nuevos del evento",
                                    required = true) @Valid @RequestPart("event") SubmitEventRequest request,
                         @Parameter(description = "Cartel nuevo (opcional)") @RequestPart(value = "poster",
                                                                                          required = false) MultipartFile poster,
                         @Parameter(description = "Si es true y no se envía poster, elimina el cartel existente") @RequestParam(defaultValue = "false") boolean removePoster);

    @PostMapping("/{eventId}/approve")
    @Operation(summary = "Aprobar eventos pendientes de moderación", description = "Pasa a APPROVED un evento en PENDING_MODERATION.")
    @ApiResponse(responseCode = "200", description = "Evento aprobado con éxito")
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiConflict
    EventPrivateDto approve(@Parameter(description = "ID del evento",
                                       example = "cccccccc-0000-0000-0000-000000000001",
                                       required = true) @PathVariable UUID eventId,
                            @Parameter(description = "Mensaje (si procede)") @Valid @RequestBody(required = false) ModerationApproveRequest request);

    @PostMapping("/{eventId}/reject")
    @Operation(summary = "Rechazar un evento", description = "Pasa a REJECTED un evento en PENDING_MODERATION o APPROVED.")
    @ApiResponse(responseCode = "200", description = "Evento rechazado con éxito")
    @ApiBadRequest
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiConflict
    EventPrivateDto reject(@Parameter(description = "ID del evento",
                                      example = "cccccccc-0000-0000-0000-000000000001",
                                      required = true) @PathVariable UUID eventId,
                           @Parameter(description = "Motivo de rechazo",
                                      required = true) @Valid @RequestBody ModerationArchiveRequest request);

    @PostMapping("/{eventId}/hide")
    @Operation(summary = "Ocultar un evento", description = "Pasa a HIDDEN un evento en PENDING_MODERATION o APPROVED.")
    @ApiResponse(responseCode = "200", description = "Evento ocultado con éxito")
    @ApiBadRequest
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiConflict
    EventPrivateDto hide(@Parameter(description = "ID del evento",
                                    example = "cccccccc-0000-0000-0000-000000000001",
                                    required = true) @PathVariable UUID eventId,
                         @Parameter(description = "Motivo de ocultación",
                                    required = true) @Valid @RequestBody ModerationArchiveRequest request);

    @PostMapping("/{eventId}/request-changes")
    @Operation(summary = "Devolver evento al autor solicitando cambios",
               description = "Pasa a NEEDS_CHANGES un evento en PENDING_MODERATION o APPROVED.")
    @ApiResponse(responseCode = "200", description = "Evento cambiado de estado con éxito")
    @ApiBadRequest
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiConflict
    EventPrivateDto requestChanges(@Parameter(description = "ID del evento",
                                              example = "cccccccc-0000-0000-0000-000000000001",
                                              required = true) @PathVariable UUID eventId,
                                   @Parameter(description = "Cambios solicitados",
                                              required = true) @Valid @RequestBody ModerationArchiveRequest request);
}
