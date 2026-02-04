package com.jmarfildev.rockalendar.moderation.api.doc;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.jmarfildev.rockalendar.common.doc.PageMetadataDoc;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedDto;

/**
 * @author jmarfil
 *
 */
@Schema(name = "ModerationArchived", description = "Respuesta paginada de eventos archivados tras moderación")
public record ModerationArchivedPageDoc(@Schema(description = "Lista de eventos") List<ModerationArchivedDto> content,
                                             @Schema(description = "Metadatos de paginación") PageMetadataDoc page) {}
