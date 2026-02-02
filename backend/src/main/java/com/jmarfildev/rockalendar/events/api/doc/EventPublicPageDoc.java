package com.jmarfildev.rockalendar.events.api.doc;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.jmarfildev.rockalendar.common.doc.PageMetadataDoc;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicDto;

/**
 * Clase para configurar la documentación OpenApi del DTO EventPubliDto.
 *
 * @author jmarfil
 *
 */
@Schema(name = "EventPublicPage", description = "Respuesta paginada de eventos públicos")
public record EventPublicPageDoc(@Schema(description = "Lista de eventos") List<EventPublicDto> content,
                                 @Schema(description = "Metadatos de paginación") PageMetadataDoc page) {}
