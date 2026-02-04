package com.jmarfildev.rockalendar.common.doc;

import java.time.OffsetDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author jmarfil
 *
 */
@Schema(name = "ValidationProblemDetail")
public record ProblemDetailValidationDoc(String type,
                                         String title,
                                         Integer status,
                                         String detail,
                                         String instance,
                                         OffsetDateTime timestamp,
                                         Map<String, String> errors) {}
