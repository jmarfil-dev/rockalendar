package com.jmarfildev.rockalendar.notifications.api;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.jmarfildev.rockalendar.common.annotations.ApiForbidden;
import com.jmarfildev.rockalendar.common.dto.PageResponse;
import com.jmarfildev.rockalendar.common.annotations.ApiNotFound;
import com.jmarfildev.rockalendar.common.annotations.ApiUnauthorized;
import com.jmarfildev.rockalendar.notifications.api.doc.NotificationPageDoc;
import com.jmarfildev.rockalendar.notifications.api.dto.NotificationDto;
import com.jmarfildev.rockalendar.notifications.api.dto.UnreadCountDto;
import com.jmarfildev.rockalendar.notifications.domain.NotificationType;

/**
 * @author jmarfil
 */
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notificaciones in-app del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public interface NotificationApi {

    @GetMapping
    @Operation(summary = "Listar notificaciones",
               description = "Devuelve las notificaciones del usuario autenticado, ordenadas por fecha descendente. "
                       + "Filtro por `bandeja` (resuelve automáticamente los tipos de esa bandeja) o por `types` para selección fina. "
                       + "Si se especifican ambos, `bandeja` tiene precedencia.")
    @ApiResponse(responseCode = "200",
                 description = "Listado paginado de notificaciones",
                 content = @Content(schema = @Schema(implementation = NotificationPageDoc.class)))
    @ApiUnauthorized
    PageResponse<NotificationDto> list(@Parameter(description = "Bandeja (USER, MODERATION, ADMIN): resuelve automáticamente los tipos correspondientes") @RequestParam(required = false) NotificationType.Bandeja bandeja,
                               @Parameter(description = "Filtro explícito por tipos (ignorado si se especifica bandeja)") @RequestParam(required = false) List<NotificationType> types,
                               @PageableDefault(size = 20) Pageable pageable);

    @GetMapping("/unread-count")
    @Operation(summary = "Contador de notificaciones no leídas",
               description = "Devuelve el número de notificaciones no leídas del usuario autenticado. Pensado para polling periódico.")
    @ApiResponse(responseCode = "200",
                 description = "Número de notificaciones no leídas",
                 content = @Content(schema = @Schema(implementation = UnreadCountDto.class)))
    @ApiUnauthorized
    UnreadCountDto unreadCount();

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marcar una notificación como leída",
               description = "Solo el destinatario de la notificación puede marcarla como leída.")
    @ApiResponse(responseCode = "204", description = "Notificación marcada como leída")
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    void markAsRead(@Parameter(description = "ID de la notificación", required = true) @PathVariable UUID id);

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marcar todas las notificaciones como leídas",
               description = "Marca como leídas todas las notificaciones no leídas del usuario autenticado.")
    @ApiResponse(responseCode = "204", description = "Todas las notificaciones marcadas como leídas")
    @ApiUnauthorized
    void markAllAsRead();
}
