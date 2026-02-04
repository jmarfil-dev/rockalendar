package com.jmarfildev.rockalendar.common.doc;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author jmarfil
 *
 */
@Schema(description = "Metadatos de paginación")
public record PageMetadataDoc(@Schema(example = "20") int size,
                              @Schema(example = "0") int number,
                              @Schema(example = "123") long totalElements,
                              @Schema(example = "7") int totalPages) {}
