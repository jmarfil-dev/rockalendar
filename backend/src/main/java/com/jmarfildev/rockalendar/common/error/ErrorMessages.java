package com.jmarfildev.rockalendar.common.error;

import lombok.NoArgsConstructor;

import com.jmarfildev.rockalendar.common.Constants;

/**
 * @author jmarfil
 *
 */
@NoArgsConstructor
public final class ErrorMessages {
    // Not found
    public static final String EVENT_NOT_FOUND = "Event not found";

    // Bad request
    public static final String ARTIST_REQUIRED = "At least one artist is required";
    public static final String ARTIST_ALREADY_EXISTS = "Artist already exists";
    public static final String INVALID_DATE_RANGE = "Invalid date range: dateFrom must be <= dateTo";
    public static final String PAGE_SIZE_TOO_LARGE = "page size exceeds the maximum allowed limit: " + Constants.maxPageSize;

    // Unauthorized
    public static final String INVALID_CREDENTIALS = "Incorrect email or password";
}
