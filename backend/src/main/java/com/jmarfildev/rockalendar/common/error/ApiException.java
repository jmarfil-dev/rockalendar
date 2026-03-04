package com.jmarfildev.rockalendar.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * @author jmarfil
 *
 */
@Getter
public abstract class ApiException extends RuntimeException implements IApiException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;
    private final String code;
    private final String type;

    protected ApiException(HttpStatus status, String code, String type) {
        super(code); // Para logs
        this.status = status;
        this.code = code;
        this.type = type;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}
