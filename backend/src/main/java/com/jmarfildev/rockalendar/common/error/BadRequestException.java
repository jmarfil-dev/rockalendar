package com.jmarfildev.rockalendar.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * @author jmarfil
 *
 */
@Getter
public class BadRequestException extends ApiException {
    private static final long serialVersionUID = 1L;

    public BadRequestException(String code) {
        super(HttpStatus.BAD_REQUEST, code, ErrorConstants.TYPE_400_BAD_REQUEST);
    }

    public BadRequestException(String code, String type) {
        super(HttpStatus.BAD_REQUEST, code, type);
    }
}
