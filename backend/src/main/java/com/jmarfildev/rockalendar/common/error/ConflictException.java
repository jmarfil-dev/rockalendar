package com.jmarfildev.rockalendar.common.error;

import org.springframework.http.HttpStatus;

/**
 * @author jmarfil
 *
 */
public class ConflictException extends ApiException {
    private static final long serialVersionUID = 1L;

    public ConflictException(String code) {
        super(HttpStatus.CONFLICT, code, ErrorConstants.TYPE_409_CONFLICT);
    }

    public ConflictException(String code, String type) {
        super(HttpStatus.CONFLICT, code, type);
    }
}
