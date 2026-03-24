package com.jmarfildev.rockalendar.common.error;

import org.springframework.http.HttpStatus;

/**
 * @author jmarfil
 *
 */
public class StorageException extends ApiException {
    private static final long serialVersionUID = 1L;

    public StorageException(String code) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code, ErrorConstants.TYPE_422_STORAGE);
    }

    public StorageException(String code, Throwable cause) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code, ErrorConstants.TYPE_422_STORAGE);
        initCause(cause);
    }
}
