package com.jmarfildev.rockalendar.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import com.jmarfildev.rockalendar.common.doc.ProblemDetailDoc;

/**
 * @author jmarfil
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ApiResponse(responseCode = "409", description = "Conflict",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailDoc.class)))
public @interface ApiConflict {}
