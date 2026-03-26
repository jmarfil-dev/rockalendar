package com.jmarfildev.rockalendar.artists.api.doc;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.jmarfildev.rockalendar.artists.api.dto.ArtistDto;
import com.jmarfildev.rockalendar.common.doc.PageMetadataDoc;

/**
 * @author jmarfil
 *
 */
@Schema(name = "ArtistPage", description = "Respuesta paginada de artistas")
public record ArtistPageDoc(@Schema(description = "Lista de artistas") List<ArtistDto> content,
                                  @Schema(description = "Metadatos de paginación") PageMetadataDoc page) {}
