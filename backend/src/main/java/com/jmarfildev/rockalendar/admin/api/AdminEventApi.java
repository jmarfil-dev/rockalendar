package com.jmarfildev.rockalendar.admin.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
// Nombre completo en el uso porque org.springframework.web.bind.annotation.RequestBody (parámetro,
// usado en overrideStatus) colisionaría en nombre simple con esta anotación (a nivel de método).
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import com.jmarfildev.rockalendar.admin.api.doc.AdminEventPageDoc;
import com.jmarfildev.rockalendar.admin.api.dto.AdminEventListItemDto;
import com.jmarfildev.rockalendar.admin.api.dto.AdminStatusOverrideRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiBadRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiConflict;
import com.jmarfildev.rockalendar.common.annotations.ApiForbidden;
import com.jmarfildev.rockalendar.common.annotations.ApiNotFound;
import com.jmarfildev.rockalendar.common.annotations.ApiUnauthorized;
import com.jmarfildev.rockalendar.common.dto.PageResponse;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;
import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 */
@RequestMapping("/api/admin/events")
@Tag(name = "Admin Events", description = "Operaciones de administración sobre eventos")
@SecurityRequirement(name = "bearerAuth")
public interface AdminEventApi {

    @GetMapping
    @Operation(summary = "Listar eventos (admin)",
               description = """
                             Lista eventos futuros con filtros opcionales.
                             Por defecto devuelve todos los estados (sin filtro de estado).
                             Soporta filtrado por estado (múltiple), provincia, rango de fechas y búsqueda por título.
                             """)
    @ApiResponse(responseCode = "200",
                 description = "Página de eventos",
                 content = @Content(schema = @Schema(implementation = AdminEventPageDoc.class)))
    @ApiUnauthorized
    @ApiForbidden
    @ApiBadRequest
    PageResponse<AdminEventListItemDto> listEvents(@Parameter(description = "Estados a incluir (por defecto: todos)") @RequestParam(required = false) List<
            EventStatus> statuses,
                                           @Parameter(description = "Código INE de provincia") @RequestParam(required = false) Optional<
                                                   Short> provinceId,
                                           @Parameter(description = "Fecha inicio (inclusive)") @RequestParam(required = false) Optional<
                                                   OffsetDateTime> dateFrom,
                                           @Parameter(description = "Fecha fin (inclusive)") @RequestParam(required = false) Optional<
                                                   OffsetDateTime> dateTo,
                                           @Parameter(description = "Búsqueda por título") @RequestParam(required = false) Optional<
                                                   String> q,
                                           @PageableDefault(size = 20, sort = "date") Pageable pageable);

    @GetMapping("/{eventId}")
    @Operation(summary = "Obtener detalle de un evento (admin)",
               description = "Devuelve el detalle completo de cualquier evento, independientemente de su estado.")
    @ApiResponse(responseCode = "200",
                 description = "Detalle del evento",
                 content = @Content(schema = @Schema(implementation = EventPrivateDto.class)))
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    EventPrivateDto getEventDetail(@Parameter(description = "ID del evento",
                                              example = "cccccccc-0000-0000-0000-000000000001",
                                              required = true) @PathVariable UUID eventId);

    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(encoding = @Encoding(name = "event", contentType = "application/json")))
    @Operation(summary = "Editar datos de cualquier evento",
               description = """
                             Permite al administrador editar los datos de un evento en estado
                             PENDING_MODERATION, NEEDS_CHANGES o APPROVED.

                             No cambia el estado del evento. Registra una acción ADMIN_EDITED en el historial de moderación.

                             El parámetro dateTbd marca la fecha del evento como no confirmada (p. ej. un
                             aplazamiento sin fecha nueva todavía). Permite guardar una fecha de inicio pasada
                             y el evento no desaparece de los listados/búsqueda pública por tener fecha vencida.
                             """)
    @ApiResponse(responseCode = "200",
                 description = "Evento actualizado correctamente",
                 content = @Content(schema = @Schema(implementation = EventPrivateDto.class)))
    @ApiBadRequest
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiConflict
    EventPrivateDto edit(@Parameter(description = "ID del evento",
                                    example = "cccccccc-0000-0000-0000-000000000001",
                                    required = true) @PathVariable UUID eventId,
                         @Parameter(description = "Datos nuevos del evento",
                                    required = true) @Valid @RequestPart("event") SubmitEventRequest request,
                         @Parameter(description = "Cartel nuevo (opcional)") @RequestPart(value = "poster",
                                                                                          required = false) MultipartFile poster,
                         @Parameter(description = "Si es true y no se envía poster, elimina el cartel existente") @RequestParam(defaultValue = "false") boolean removePoster,
                         @Parameter(description = "Comentario opcional del administrador para el historial de auditoría") @RequestParam(required = false) @Size(max = 500) String comment,
                         @Parameter(description = "Marca la fecha del evento como no confirmada (aplazado sin fecha nueva)") @RequestParam(defaultValue = "false") boolean dateTbd);

    @PostMapping("/{eventId}/status")
    @Operation(summary = "Forzar cambio de estado de un evento",
               description = """
                             Permite a un administrador cambiar el estado de cualquier evento a cualquier otro estado,
                             sin restricciones de flujo de moderación.

                             Restricción: ERASED es un estado terminal e irreversible — no se puede transicionar
                             desde ni hacia ERASED.

                             Registra una acción ADMIN_STATE_OVERRIDE en el historial de moderación.
                             """)
    @ApiResponse(responseCode = "200",
                 description = "Estado actualizado correctamente",
                 content = @Content(schema = @Schema(implementation = EventPrivateDto.class)))
    @ApiBadRequest
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiConflict
    EventPrivateDto overrideStatus(@Parameter(description = "ID del evento",
                                              example = "cccccccc-0000-0000-0000-000000000001",
                                              required = true) @PathVariable UUID eventId,
                                   @Valid @RequestBody AdminStatusOverrideRequest request);
}
