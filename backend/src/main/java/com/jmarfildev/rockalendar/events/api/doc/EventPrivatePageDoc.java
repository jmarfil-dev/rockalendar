package com.jmarfildev.rockalendar.events.api.doc;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.jmarfildev.rockalendar.common.doc.PageMetadataDoc;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateDto;

/**
 * @author jmarfil
 *
 */
@Schema(name = "EventPrivatePage", description = "Respuesta paginada de eventos del usuario autenticado")
public record EventPrivatePageDoc(@Schema(description = "Lista de eventos") List<EventPrivateDto> content,
                                  @Schema(description = "Metadatos de paginación") PageMetadataDoc page) {}
