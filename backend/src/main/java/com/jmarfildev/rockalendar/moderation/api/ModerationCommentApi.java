package com.jmarfildev.rockalendar.moderation.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

import com.jmarfildev.rockalendar.common.annotations.ApiForbidden;
import com.jmarfildev.rockalendar.common.annotations.ApiNotFound;
import com.jmarfildev.rockalendar.common.annotations.ApiUnauthorized;
import com.jmarfildev.rockalendar.events.api.dto.EventCommentDto;

/**
 * @author jmarfil
 */
@RequestMapping("/api/moderation/events")
@Tag(name = "Moderation Events", description = "Operaciones de moderación de eventos")
@SecurityRequirement(name = "bearerAuth")
public interface ModerationCommentApi {

    @GetMapping("/{eventId}/comments")
    @Operation(summary = "Listar comentarios de un evento",
               description = "Devuelve todos los comentarios de usuarios sobre un evento, ordenados por fecha.")
    @ApiResponse(responseCode = "200",
                 description = "Lista de comentarios",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = EventCommentDto.class))))
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    List<EventCommentDto> listComments(@Parameter(description = "ID del evento", required = true) @PathVariable UUID eventId);

    @DeleteMapping("/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un comentario de un evento")
    @ApiResponse(responseCode = "204", description = "Comentario eliminado")
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    void deleteComment(@Parameter(description = "ID del evento", required = true) @PathVariable UUID eventId,
                       @Parameter(description = "ID del comentario", required = true) @PathVariable UUID commentId);
}
