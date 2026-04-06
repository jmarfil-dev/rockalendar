package com.jmarfildev.rockalendar.admin.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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

/**
 * @author jmarfil
 */
@RequestMapping("/api/admin/events")
@Tag(name = "Admin Events", description = "Operaciones de administración sobre eventos")
@SecurityRequirement(name = "bearerAuth")
public interface AdminEventApi {

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
