package com.jmarfildev.rockalendar.common.error;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

import com.jmarfildev.rockalendar.auth.application.AuthService;

/**
 * @author jmarfil
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation error");
        pd.setDetail("Request validation failed");
        pd.setType(URI.create("urn:rockalendar:error:validation"));
        pd.setProperty("timestamp", OffsetDateTime.now());

        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage(),
                        (a, b) -> a));
        pd.setProperty("errors", errors);

        return pd;
    }

    @ExceptionHandler({
                        BadRequestException.class,
                        MethodArgumentTypeMismatchException.class,
                        ConversionFailedException.class
    })
    public ProblemDetail handleBadRequest(Exception ex) {
        /*
         * BadRequestException es una excepción custom y se controla el mensaje al lanzarla
         * Las otras excepciones tienen mensajes de sistema que no deben exponerse
         */
        String detail = ex instanceof BadRequestException bre
                ? bre.getMessage()
                : "Invalid value for request parameter";

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
        pd.setDetail(detail);
        pd.setType(URI.create("urn:rockalendar:error:bad-request"));
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    /**
     * Para capturar la excepción que lanza {@link AuthService}.
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        pd.setDetail(ErrorMessages.INVALID_CREDENTIALS);
        pd.setType(URI.create("urn:rockalendar:error:unauthorized"));
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle(HttpStatus.NOT_FOUND.getReasonPhrase());
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("urn:rockalendar:error:not-found"));
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle(HttpStatus.FORBIDDEN.getReasonPhrase());
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("urn:rockalendar:error:forbidden"));
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
