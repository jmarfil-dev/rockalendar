package com.jmarfildev.rockalendar.common.error;

import org.springframework.http.HttpStatus;

/**
 * @author jmarfil
 *
 */
public interface IApiException {
    HttpStatus getStatus();

    String getCode();

    String getType();
}
