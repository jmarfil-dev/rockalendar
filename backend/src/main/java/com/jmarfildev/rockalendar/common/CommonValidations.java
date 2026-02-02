package com.jmarfildev.rockalendar.common;

import org.springframework.data.domain.Pageable;

import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;

/**
 * @author jmarfil
 *
 */
public class CommonValidations {

    public static void validatePageable(Pageable pageable) {
        if (pageable.getPageSize() > Constants.maxPageSize) {
            throw new BadRequestException(ErrorMessages.PAGE_SIZE_TOO_LARGE);
        }
    }
}
