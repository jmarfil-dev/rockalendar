package com.jmarfildev.rockalendar.common.error;

import lombok.Getter;

/**
 * @author jmarfil
 *
 */
@Getter
public class BadRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String type;

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, String type) {
        super(message);
        this.type = type;
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
