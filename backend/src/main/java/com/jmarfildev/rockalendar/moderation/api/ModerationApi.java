package com.jmarfildev.rockalendar.moderation.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.jmarfildev.rockalendar.common.annotations.ApiForbidden;
import com.jmarfildev.rockalendar.common.annotations.ApiUnauthorized;
import com.jmarfildev.rockalendar.moderation.api.doc.ModerationArchivedPageDoc;
import com.jmarfildev.rockalendar.moderation.api.doc.ModerationPendingPageDoc;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingDto;

/**
 * @author jmarfil
 *
 */
@RequestMapping("/api/moderation/events")
@Tag(name = "Moderation Events", description = "Operaciones de moderación de eventos")
@SecurityRequirement(name = "bearerAuth")
public interface ModerationApi {

    @GetMapping("/pending")
    @Operation(summary = "Listar eventos pendientes de moderación",
            description = "Devuelve eventos en estado PENDING_MODERATION. Listado ligero para revisión.")
    @ApiResponse(responseCode = "200", description = "Listado paginado de eventos pendientes",
            content = @Content(schema = @Schema(implementation = ModerationPendingPageDoc.class)))
    @ApiUnauthorized
    @ApiForbidden
    Page<ModerationPendingDto> listPending(@PageableDefault(size = 20) Pageable pageable);

    @GetMapping("/archived")
    @Operation(summary = "Listar eventos archivados de moderación",
            description = "Devuelve eventos en estado REJECTED, HIDDEN o CANCELED. Incluye estado y último mensaje de moderación.")
    @ApiResponse(responseCode = "200", description = "Listado paginado de eventos archivados",
            content = @Content(schema = @Schema(implementation = ModerationArchivedPageDoc.class)))
    @ApiUnauthorized
    @ApiForbidden
    Page<ModerationArchivedDto> listArchived(@PageableDefault(size = 20) Pageable pageable);
}
