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

import com.jmarfildev.rockalendar.common.annotations.ApiBadRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiNotFound;
import com.jmarfildev.rockalendar.events.api.doc.EventPublicPageDoc;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicListItemDto;

/**
 * @author jmarfil
 *
 */
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Consulta de eventos públicos")
public interface EventApi {

    @GetMapping("/home")
    @Operation(summary = "Home público: próximos eventos",
               description = """
                             Lista eventos públicos (APPROVED) a partir de la fecha actual. Devuelve lo mismo que "/api/events" sin filtros, pero con una consulta más ligera y sin eventos pasados.

                             La ordenación por pageable permite direcciones asc y desc, y los campos title, date (fecha de inicio),
                             province y city. Ignora cualquier valor distinto.


                             """)
    @ApiResponse(responseCode = "200",
                 description = "Página de eventos públicos",
                 content = @Content(schema = @Schema(implementation = EventPublicPageDoc.class)))
    Page<EventPublicListItemDto> listHome(@Parameter(description = "Paginación (page, size, sort)") @PageableDefault(size = 20) Pageable pageable);

    @GetMapping
    @Operation(summary = "Buscar eventos públicos",
            description = """
                    Búsqueda flexible de eventos públicos (APPROVED) con tolerancia a errores tipográficos.

                    La búsqueda combina:
                    - Full Text Search (FTS) para coincidencias exactas y relevantes.
                    - Búsqueda tolerante (trigram similarity) para prefijos y errores de escritura.
                    - Filtros exactos por provincia, ciudad y artista.

                    Reglas importantes:
                    - Solo se devuelven eventos con estado APPROVED.
                    - Los filtros por ciudad y artista son exactos (por slug).
                    - La tolerancia a errores se aplica únicamente al parámetro `query`.
                    - Si `query` está vacío, se listan eventos según los filtros y el orden por defecto.
                    """)
    @ApiResponse(responseCode = "200", description = "Página de eventos públicos",
            content = @Content(schema = @Schema(implementation = EventPublicPageDoc.class)))
    @ApiBadRequest
    Page<EventPublicDto> searchPublic(@Parameter(description = "Búsqueda libre (título, sala, ciudad, artista)",
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
    @ApiNotFound
    EventPublicDto getPublicById(@Parameter(description = "ID del evento", example = "cccccccc-0000-0000-0000-000000000001",
            required = true) @PathVariable UUID id);
}
