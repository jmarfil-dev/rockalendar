package com.jmarfildev.rockalendar.common.error;

import lombok.NoArgsConstructor;

/**
 * @author jmarfil
 *
 */
@NoArgsConstructor
public final class ErrorConstants {
    /*
     *  Types
     */
    public static final String TYPE_400_VALIDATION = "validation";
    public static final String TYPE_400_BAD_REQUEST = "bad-request";
    public static final String TYPE_401_UNAUTHORIZED = "unauthorized";
    public static final String TYPE_403_FORBIDDEN = "forbidden";
    public static final String TYPE_404_NOT_FOUND = "not-found";
    public static final String TYPE_409_CONFLICT = "conflict";
    public static final String TYPE_409_MODERATION_STATE = "moderation-state";
    public static final String TYPE_409_EVENT_STATE = "event-state";
    public static final String TYPE_500_SERVER = "internal-error";

    /*
     * Codes
     */
    // Bad request 400
    public static final String VALIDATION_ERROR = "error.400.genericValidation";
    public static final String REQUEST_VALID_ERROR = "error.400.reqValidError";
    public static final String REQUEST_REQUIRED = "error.400.reqRequired";
    public static final String CITY_REQUIRED = "error.400.cityRequired";
    public static final String VENUE_REQUIRED = "error.400.venueRequired";
    public static final String TITLE_REQUIRED = "error.400.titleRequired";
    public static final String ARTIST_REQUIRED = "error.400.artistRequired";
    public static final String REASON_REQUIRED = "error.400.reasonRequired";
    public static final String INVALID_PROVINCE = "error.400.invalidProvince";
    public static final String INVALID_DATE_RANGE = "error.400.invalidDateRange";
    public static final String PAGE_SIZE_TOO_LARGE = "error.400.pageSizeTooLarge";
    public static final String VALID_PASSWORD = "error.400.valid.patternPassword";
    public static final String VALID_SIZE_LIST_EMPTY = "error.400.valid.sizeListEmpty";
    public static final String VALID_SIZE_PASSWORD = "error.400.valid.sizePassword";

    // Unauthorized 401
    public static final String AUTH_REQUIRED = "error.401.message";
    public static final String INVALID_CREDENTIALS = "error.401.invalidCredentials";

    // Forbidden 403
    public static final String ACCESS_DENIED = "error.403.message";
    public static final String EVENT_NOT_OWNER = "error.403.eventNotOwner";

    // Not found 404
    public static final String EVENT_NOT_FOUND = "error.404.eventNotFound";
    public static final String ARTIST_NOT_FOUND = "error.404.artistNotFound";

    // Conflict 409
    public static final String ARTIST_ALREADY_EXISTS = "error.409.artistExists";
    public static final String EMAIL_ALREADY_EXISTS = "error.409.emailExists";
    public static final String EVENT_NOT_PENDING = "error.409.eventNotPending";
    public static final String MODERATOR_OWN = "error.409.moderatorOwn";
    public static final String EVENT_ALREADY_MOD = "error.409.eventAlreadyMod";
    public static final String EVENT_NOT_EDITABLE = "error.409.eventNotEditable";
    public static final String EVENT_NOT_ERASABLE = "error.409.eventNotErasable";
    public static final String EVENT_NOT_ERASABLE_APPROVED = "error.409.eventNotErasableAppr";

    public static final String AGENDA_EVENT_NOT_AVAILABLE = "error.409.agendaEventNotAvailable";

    // Conflict 409 genérico (constraint de BD no capturada explícitamente)
    public static final String DB_CONSTRAINT = "error.409.dbConstraint";

    // Server Error 500
    public static final String SERVER = "error.500.server";

    /*
     * Titles
     */
    public static final String TIT_VALIDATION_ERROR = "Validation error";
}
