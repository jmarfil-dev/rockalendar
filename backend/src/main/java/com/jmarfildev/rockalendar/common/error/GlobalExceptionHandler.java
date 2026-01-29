package com.jmarfildev.rockalendar.common.error;

import java.net.URI;
import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author jmarfil
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle(HttpStatus.NOT_FOUND.getReasonPhrase());
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("urn:rockalendar:error:not-found"));
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("urn:rockalendar:error:bad-request"));
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle(HttpStatus.CONFLICT.getReasonPhrase());
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("urn:rockalendar:error:conflict"));
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    /**
     * Si en algún punto se usa ResponseStatusException u otras ErrorResponseException,
     * esto mantiene una salida consistente.
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
        ProblemDetail pd = ex.getBody();
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }
}
