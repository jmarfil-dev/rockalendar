package com.jmarfildev.rockalendar.common.error;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.auth.application.AuthService;

/**
 * @author jmarfil
 *
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage(),
                        (a, b) -> a));
        pd.setProperty("errors", errors);

        return ProblemDetailGenericProperties.setGenericProperties(pd, ErrorMessages.VALIDATION_ERROR, "Request validation failed",
                req.getRequestURI(), ErrorMessages.TYPE_400_VALIDATION);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        return ProblemDetailGenericProperties.setGenericProperties(pd, ErrorMessages.VALIDATION_ERROR, "Request body is required",
                req.getRequestURI(), ErrorMessages.TYPE_400_VALIDATION);
    }

    @ExceptionHandler({
                        MethodArgumentTypeMismatchException.class,
                        ConversionFailedException.class
    })
    public ProblemDetail handleBadRequest(RuntimeException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        String detail = "Invalid value for request parameter";

        if (ex instanceof MethodArgumentTypeMismatchException matme) {
            detail = detail + " '%s'".formatted(matme.getName());
            pd.setProperty("parameter", matme.getName());
            if (matme.getRequiredType() != null) {
                pd.setProperty("expectedType", matme.getRequiredType().getSimpleName());
            }
        }

        return ProblemDetailGenericProperties.setGenericProperties(pd, ErrorMessages.VALIDATION_ERROR, detail,
                req.getRequestURI(), ErrorMessages.TYPE_400_VALIDATION);
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        return ProblemDetailGenericProperties.setGenericProperties(pd, HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage(),
                req.getRequestURI(), ex.getType() != null ? ex.getType() : ErrorMessages.TYPE_400_BAD_REQUEST);
    }

    /**
     * Para capturar la excepción que lanza {@link AuthService}.
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        return ProblemDetailGenericProperties.setGenericProperties(pd, HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage(),
                req.getRequestURI(), ErrorMessages.TYPE_401_UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        return ProblemDetailGenericProperties.setGenericProperties(pd, HttpStatus.FORBIDDEN.getReasonPhrase(), ex.getMessage(),
                req.getRequestURI(), ErrorMessages.TYPE_403_FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        return ProblemDetailGenericProperties.setGenericProperties(pd, HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(),
                req.getRequestURI(), ErrorMessages.TYPE_404_NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        return ProblemDetailGenericProperties.setGenericProperties(pd, HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage(),
                req.getRequestURI(), ex.getType() != null ? ex.getType() : ErrorMessages.TYPE_409_CONFLICT);
    }

    /**
     * Si en algún punto se usa ResponseStatusException u otras ErrorResponseException,
     * esto mantiene una salida consistente.
     *
     * @param ex
     * @param req
     * @return
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
        ProblemDetail pd = ex.getBody();
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    /**
     * catch-all para tener un type estable (internal-error) que el frontend puede manejar (“algo fue mal”).
     *
     * @param ex
     * @param req
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest req) {
        String errorId = UUID.randomUUID().toString();
        log.error("errorId={} Unexpected error on {} {}", errorId, req.getMethod(), req.getRequestURI(), ex);

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setProperty("errorId", errorId);
        return ProblemDetailGenericProperties.setGenericProperties(pd, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unexpected server error", req.getRequestURI(), "internal-error");
    }
}
