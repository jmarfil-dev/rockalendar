package com.jmarfildev.rockalendar.admin.api;

import java.util.UUID;

import org.springframework.http.MediaType;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.jmarfildev.rockalendar.admin.api.dto.AdminStatusOverrideRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiBadRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiConflict;
import com.jmarfildev.rockalendar.common.annotations.ApiForbidden;
import com.jmarfildev.rockalendar.common.annotations.ApiNotFound;
import com.jmarfildev.rockalendar.common.annotations.ApiUnauthorized;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;
import com.jmarfildev.rockalendar.events.api.dto.SubmitEventRequest;

/**
 * @author jmarfil
 */
@RequestMapping("/api/admin/events")
@Tag(name = "Admin Events", description = "Operaciones de administración sobre eventos")
@SecurityRequirement(name = "bearerAuth")
public interface AdminEventApi {

    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Editar datos de cualquier evento",
               description = """
                             Permite al administrador editar los datos de un evento en estado
                             PENDING_MODERATION, NEEDS_CHANGES o APPROVED.

                             No cambia el estado del evento. Registra una acción ADMIN_EDITED en el historial de moderación.
                             """)
    @ApiResponse(responseCode = "200", description = "Evento actualizado correctamente")
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
                         @Parameter(description = "Si es true y no se envía poster, elimina el cartel existente") @RequestParam(defaultValue = "false") boolean removePoster);

    @PostMapping("/{eventId}/status")
    @Operation(summary = "Forzar cambio de estado de un evento",
               description = """
                             Permite a un administrador cambiar el estado de cualquier evento a cualquier otro estado,
                             sin restricciones de flujo de moderación.

                             Restricción: ERASED es un estado terminal e irreversible — no se puede transicionar
                             desde ni hacia ERASED.

                             Registra una acción ADMIN_STATE_OVERRIDE en el historial de moderación.
                             """)
    @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente")
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
