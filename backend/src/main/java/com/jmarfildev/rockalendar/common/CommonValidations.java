package com.jmarfildev.rockalendar.common;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.ForbiddenException;

/**
 * @author jmarfil
 *
 */
public class CommonValidations {
    private CommonValidations() {}

    public static void validatePageable(Pageable pageable) {
        if (pageable.getPageSize() > Constants.MAX_PAGE_SIZE) {
            throw new BadRequestException(ErrorConstants.PAGE_SIZE_TOO_LARGE);
        }
    }

    /**
     * Lanza {@link BadRequestException} si {@code to} no es null y es anterior a {@code from}.
     */
    public static void validateDateRange(OffsetDateTime from, OffsetDateTime to) {
        if (to != null && to.isBefore(from)) {
            throw new BadRequestException(ErrorConstants.INVALID_DATE_RANGE);
        }
    }

    /**
     * Lanza {@link BadRequestException} si {@code endDate} no es null y es anterior a {@code startDate}.
     */
    public static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException(ErrorConstants.INVALID_DATE_RANGE);
        }
    }

    /**
     * Lanza {@link ForbiddenException} si {@code userId} no coincide con el propietario del evento.
     */
    public static void validateEventOwner(UUID userId, UUID ownerId) {
        if (!userId.equals(ownerId)) {
            throw new ForbiddenException(ErrorConstants.EVENT_NOT_OWNER);
        }
    }
}
