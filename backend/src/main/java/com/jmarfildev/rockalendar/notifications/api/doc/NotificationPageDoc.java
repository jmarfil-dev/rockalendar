package com.jmarfildev.rockalendar.notifications.api.doc;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.jmarfildev.rockalendar.common.doc.PageMetadataDoc;
import com.jmarfildev.rockalendar.notifications.api.dto.NotificationDto;

/**
 * @author jmarfil
 */
@Schema(name = "NotificationPage", description = "Respuesta paginada de notificaciones")
public record NotificationPageDoc(
        @Schema(description = "Lista de notificaciones") List<NotificationDto> content,
        @Schema(description = "Metadatos de paginación") PageMetadataDoc page) {}
