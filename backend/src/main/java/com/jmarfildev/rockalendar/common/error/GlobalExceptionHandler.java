package com.jmarfildev.rockalendar.common.error;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.StaleObjectStateException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
                       .collect(Collectors.toMap(fe -> fe.getField(),
                                                 fe -> fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage(),
                                                 (a, b) -> a));
        pd.setProperty("errors", errors);

        return build(pd, ErrorConstants.TIT_VALIDATION_ERROR, ErrorConstants.REQUEST_VALID_ERROR, req.getRequestURI(),
                     ErrorConstants.TYPE_400_VALIDATION);
    }

    @ExceptionHandler({ HttpMessageNotReadableException.class, NoResourceFoundException.class, MissingServletRequestPartException.class })
    public ProblemDetail handleHttpMessageNotReadable(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        return build(pd, ErrorConstants.TIT_VALIDATION_ERROR, ErrorConstants.REQUEST_REQUIRED, req.getRequestURI(),
                     ErrorConstants.TYPE_400_VALIDATION);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, ConversionFailedException.class })
    public ProblemDetail handleBadRequest(RuntimeException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        String detail = "Invalid value for request parameter";

        if (ex instanceof MethodArgumentTypeMismatchException matme) {
            detail += " '%s'".formatted(matme.getName());
            pd.setProperty("parameter", matme.getName());
            Class<?> requiredType = matme.getRequiredType();
            if (requiredType != null) {
                pd.setProperty("expectedType", requiredType.getSimpleName());
            }
        }

        pd.setDetail(detail);
        // code = null para forzar mostrar detail
        return build(pd, ErrorConstants.TIT_VALIDATION_ERROR, null, req.getRequestURI(), ErrorConstants.TYPE_400_VALIDATION);
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
        return build(pd, HttpStatus.UNAUTHORIZED.getReasonPhrase(), ErrorConstants.INVALID_CREDENTIALS, req.getRequestURI(),
                     ErrorConstants.TYPE_401_UNAUTHORIZED);
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(ex.getStatus());
        return build(pd, ex.getStatus().getReasonPhrase(), ex.getCode(), req.getRequestURI(), ex.getType());
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
     * Captura modificaciones concurrentes sobre la misma entidad (optimistic locking).
     * Devuelve 409 en lugar de 500: el cliente debe reintentar la operación.
     */
    @ExceptionHandler({ ObjectOptimisticLockingFailureException.class, StaleObjectStateException.class })
    public ProblemDetail handleOptimisticLock(Exception ex, HttpServletRequest req) {
        log.warn("Optimistic lock conflict on {} {}", req.getMethod(), req.getRequestURI());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        return build(pd, HttpStatus.CONFLICT.getReasonPhrase(), ErrorConstants.EVENT_ALREADY_MOD, req.getRequestURI(),
                     ErrorConstants.TYPE_409_CONFLICT);
    }

    /**
     * Captura violaciones de constraints de BD no convertidas explícitamente en ConflictException.
     * Devuelve 409 en lugar de 500 para que el frontend pueda distinguirlo de un error de servidor.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("DataIntegrityViolationException on {} {}: {}", req.getMethod(), req.getRequestURI(),
                 ex.getMostSpecificCause().getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        return build(pd, HttpStatus.CONFLICT.getReasonPhrase(), ErrorConstants.DB_CONSTRAINT, req.getRequestURI(),
                     ErrorConstants.TYPE_409_CONFLICT);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setDetail("La imagen supera el tamaño máximo permitido (10 MB)");
        return build(pd, ErrorConstants.TIT_STORAGE_ERROR, ErrorConstants.INVALID_IMAGE, req.getRequestURI(),
                     ErrorConstants.TYPE_422_STORAGE);
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
        return build(pd, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), ErrorConstants.SERVER, req.getRequestURI(),
                     ErrorConstants.TYPE_500_SERVER);
    }

    private static ProblemDetail build(ProblemDetail pd, String title, String code, String instance, String type) {
        return ProblemDetailGenericProperties.setGenericProperties(pd, title, code, instance, type);
    }
}
