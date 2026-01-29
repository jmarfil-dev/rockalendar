package com.jmarfildev.rockalendar.events.api.doc;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.jmarfildev.rockalendar.events.api.dto.EventPublicDto;

/**
 * Clase para configurar la documentación OpenApi del DTO EventPubliDto.
 *
 * @author jmarfil
 *
 */
@Schema(name = "EventPublicPage", description = "Respuesta paginada de eventos públicos")
public record EventPublicPageDoc(@Schema(description = "Lista de eventos") List<EventPublicDto> content,
                                 @Schema(description = "Metadatos de paginación") PageMetadata page) {
    @Schema(description = "Metadatos de paginación")
    public record PageMetadata(@Schema(example = "20") int size,
                               @Schema(example = "0") int number,
                               @Schema(example = "123") long totalElements,
                               @Schema(example = "7") int totalPages) {}
}
