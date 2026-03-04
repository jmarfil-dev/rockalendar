package com.jmarfildev.rockalendar.common.error;

import org.springframework.http.HttpStatus;

/**
 * @author jmarfil
 *
 */
public class NotFoundException extends ApiException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(String code) {
        super(HttpStatus.NOT_FOUND, code, ErrorConstants.TYPE_404_NOT_FOUND);
    }
}
