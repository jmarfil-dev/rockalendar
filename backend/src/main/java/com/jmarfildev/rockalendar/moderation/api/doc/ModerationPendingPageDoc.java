package com.jmarfildev.rockalendar.moderation.api.doc;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.jmarfildev.rockalendar.common.doc.PageMetadataDoc;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingDto;

/**
 * @author jmarfil
 *
 */
@Schema(name = "ModerationPending", description = "Respuesta paginada de eventos pendientes de moderación")
public record ModerationPendingPageDoc(@Schema(description = "Lista de eventos") List<ModerationPendingDto> content,
                                            @Schema(description = "Metadatos de paginación") PageMetadataDoc page) {}
