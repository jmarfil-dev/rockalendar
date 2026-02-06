package com.jmarfildev.rockalendar.common.error;

import lombok.Getter;

/**
 * @author jmarfil
 *
 */
@Getter
public class ConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String type;

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, String type) {
        super(message);
        this.type = type;
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
