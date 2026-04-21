package com.jmarfildev.rockalendar.events.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.jmarfildev.rockalendar.common.annotations.ApiBadRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiNotFound;
import com.jmarfildev.rockalendar.events.api.dto.PostCommentRequest;

/**
 * @author jmarfil
 */
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Consulta de eventos públicos")
public interface EventCommentApi {

    @PostMapping("/{id}/comment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Enviar comentario sobre un evento",
               description = "Accesible sin autenticación. Si el usuario está autenticado, el email se toma del token. Si no, authorEmail es obligatorio.")
    @ApiResponse(responseCode = "204", description = "Comentario enviado correctamente")
    @ApiBadRequest
    @ApiNotFound
    void postComment(@Parameter(description = "ID del evento", required = true) @PathVariable UUID id,
                     @Valid @RequestBody PostCommentRequest request);
}
