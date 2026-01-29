package com.jmarfildev.rockalendar.common.error;

/**
 * @author jmarfil
 *
 */
public class ConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
