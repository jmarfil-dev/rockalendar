package com.jmarfildev.rockalendar.common.doc;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author jmarfil
 *
 */
@Schema(name = "ProblemDetail")
public record ProblemDetailDoc(String type,
                               String title,
                               Integer status,
                               String detail,
                               @Schema(description = "URI del recurso que causó el error (si aplica)") String instance,
                               OffsetDateTime timestamp) {}
