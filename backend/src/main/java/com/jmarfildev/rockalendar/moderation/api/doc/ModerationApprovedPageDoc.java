package com.jmarfildev.rockalendar.moderation.api.doc;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.jmarfildev.rockalendar.common.doc.PageMetadataDoc;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationApprovedListItemDto;

/**
 * @author jmarfil
 *
 */
@Schema(name = "ModerationApproved", description = "Respuesta paginada de eventos aprobados")
public record ModerationApprovedPageDoc(@Schema(description = "Lista de eventos") List<ModerationApprovedListItemDto> content,
                                        @Schema(description = "Metadatos de paginación") PageMetadataDoc page) {}
