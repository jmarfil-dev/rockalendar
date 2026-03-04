package com.jmarfildev.rockalendar.common.error;

import org.springframework.http.HttpStatus;

/**
 * @author jmarfil
 *
 */
public class ForbiddenException extends ApiException {
    private static final long serialVersionUID = 1L;

    public ForbiddenException(String code) {
        super(HttpStatus.FORBIDDEN, code, ErrorConstants.TYPE_403_FORBIDDEN);
    }
}
