package com.jmarfildev.rockalendar.common.error;

import lombok.NoArgsConstructor;

import com.jmarfildev.rockalendar.common.Constants;

/**
 * @author jmarfil
 *
 */
@NoArgsConstructor
public final class ErrorMessages {
    // Bad request 400
    public static final String CITY_REQUIRED = "cityName is required";
    public static final String VENUE_REQUIRED = "venueName is required";
    public static final String TITLE_REQUIRED = "title is required";
    public static final String ARTIST_REQUIRED = "At least one artist is required";
    public static final String INVALID_PROVINCE = "Invalid provinceId";
    public static final String INVALID_DATE_RANGE = "Invalid date range: dateFrom must be <= dateTo";
    public static final String INVALID_EVENT_DATE = "endDateTime must be after or equal to startDateTime";
    public static final String PAGE_SIZE_TOO_LARGE = "page size exceeds the maximum allowed limit: " + Constants.maxPageSize;

    // Unauthorized 401
    public static final String INVALID_CREDENTIALS = "Incorrect email or password";

    // Not found 404
    public static final String EVENT_NOT_FOUND = "Event not found";

    // Conflict 409
    public static final String ARTIST_ALREADY_EXISTS = "Artist already exists";
    public static final String EMAIL_ALREADY_EXISTS = "email already exists";
}
