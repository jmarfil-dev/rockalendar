package com.jmarfildev.rockalendar.admin.api.doc;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.jmarfildev.rockalendar.admin.api.dto.AdminEventListItemDto;
import com.jmarfildev.rockalendar.common.doc.PageMetadataDoc;

/**
 * @author jmarfil
 */
@Schema(name = "AdminEventPage", description = "Respuesta paginada de eventos para administración")
public record AdminEventPageDoc(@Schema(description = "Lista de eventos") List<AdminEventListItemDto> content,
                                @Schema(description = "Metadatos de paginación") PageMetadataDoc page) {}
