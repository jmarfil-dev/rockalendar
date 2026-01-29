package com.jmarfildev.rockalendar.events.api;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.jmarfildev.rockalendar.events.api.doc.EventPublicPageDoc;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicDto;

/**
 * @author jmarfil
 *
 */
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Consulta de eventos públicos")
public interface EventApi {

    @GetMapping
    @Operation(summary = "Buscar eventos públicos",
            description = """
                    Devuelve eventos públicos con estado APPROVED.

                    Reglas importantes:
                    - query: búsqueda libre tokenizada (tokens < 3 caracteres se ignoran).
                    - city: texto libre; se normaliza internamente a slug (tildes/espacios/mayúsculas).
                    - artist: texto libre; se normaliza internamente a slug.
                    """)
    @ApiResponse(responseCode = "200", description = "Página de eventos públicos",
            content = @Content(schema = @Schema(implementation = EventPublicPageDoc.class)))
    @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda inválidos")
    Page<EventPublicDto> search(@Parameter(description = "Búsqueda libre (título, sala, ciudad, artista)",
            example = "metallica madrid") @RequestParam Optional<String> query,
                                @Parameter(description = "Fecha/hora desde (ISO-8601)",
                                        example = "2026-04-01T00:00:00Z") @RequestParam @DateTimeFormat(
                                                iso = DateTimeFormat.ISO.DATE_TIME) Optional<OffsetDateTime> dateFrom,
                                @Parameter(description = "Fecha/hora hasta (ISO-8601)",
                                        example = "2026-04-30T23:59:59Z") @RequestParam @DateTimeFormat(
                                                iso = DateTimeFormat.ISO.DATE_TIME) Optional<OffsetDateTime> dateTo,
                                @Parameter(description = "Filtra por provincia",
                                        example = "11111111-1111-1111-1111-111111111111") @RequestParam Optional<UUID> provinceId,
                                @Parameter(description = "Ciudad (texto libre); se normaliza internamente a slug",
                                        example = "València") @RequestParam Optional<String> city,
                                @Parameter(description = "Artista (texto libre); se normaliza internamente a slug",
                                        example = "Iron Maiden") @RequestParam Optional<String> artist,
                                @Parameter(description = "Paginación (page, size, sort)",
                                        example = "page=0&size=20") @PageableDefault(size = 20) Pageable pageable);

    @GetMapping("/{id}")
    @Operation(summary = "Obtener evento público por ID", description = "Devuelve un evento público por su ID (solo estado APPROVED).")
    @ApiResponse(responseCode = "200", description = "Evento encontrado",
            content = @Content(schema = @Schema(implementation = EventPublicDto.class)))
    @ApiResponse(responseCode = "404", description = "Evento no encontrado o no público")
    EventPublicDto getById(@Parameter(description = "ID del evento", example = "cccccccc-0000-0000-0000-000000000001",
            required = true) @PathVariable UUID id);
}
