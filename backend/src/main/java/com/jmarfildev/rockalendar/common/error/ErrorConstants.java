package com.jmarfildev.rockalendar.common.error;

/**
 * @author jmarfil
 *
 */
public final class ErrorConstants {
    private ErrorConstants() {}

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
    public static final String TYPE_429_TOO_MANY_REQUESTS = "too-many-requests";
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
    public static final String EVENT_START_DATE_PAST = "error.400.eventStartDatePast";
    public static final String PAGE_SIZE_TOO_LARGE = "error.400.pageSizeTooLarge";
    public static final String WRONG_CURRENT_PASSWORD = "error.400.wrongCurrentPassword";
    public static final String WRONG_CONFIRM_PASSWORD = "error.400.wrongConfirmPassword";
    public static final String VALID_PASSWORD = "error.400.valid.patternPassword";
    public static final String VALID_SIZE_LIST_EMPTY = "error.400.valid.sizeListEmpty";
    public static final String VALID_SIZE_PASSWORD = "error.400.valid.sizePassword";
    public static final String PRIVACY_NOT_ACCEPTED = "error.400.valid.privacyNotAccepted";
    public static final String INVALID_RESET_TOKEN = "error.400.invalidResetToken";
    public static final String INVALID_LOCALE = "error.400.invalidLocale";

    // Unauthorized 401
    public static final String AUTH_REQUIRED = "error.401.message";
    public static final String INVALID_CREDENTIALS = "error.401.invalidCredentials";

    // Forbidden 403
    public static final String ACCESS_DENIED = "error.403.message";
    public static final String EVENT_NOT_OWNER = "error.403.eventNotOwner";
    public static final String NOTIFICATION_NOT_OWNER = "error.403.notificationNotOwner";

    // Not found 404
    public static final String EVENT_NOT_FOUND = "error.404.eventNotFound";
    public static final String ARTIST_NOT_FOUND = "error.404.artistNotFound";
    public static final String USER_NOT_FOUND = "error.404.userNotFound";
    public static final String NOTIFICATION_NOT_FOUND = "error.404.notificationNotFound";

    // Conflict 409
    public static final String ARTIST_ALREADY_EXISTS = "error.409.artistExists";
    public static final String ARTIST_HAS_EVENTS = "error.409.artistHasEvents";
    public static final String EMAIL_ALREADY_EXISTS = "error.409.emailExists";
    public static final String EVENT_NOT_PENDING = "error.409.eventNotPending";
    public static final String EVENT_ERASED_TERMINAL = "error.409.eventErasedTerminal";
    public static final String MODERATOR_OWN = "error.409.moderatorOwn";
    public static final String EVENT_ALREADY_MOD = "error.409.eventAlreadyMod";
    public static final String EVENT_NOT_EDITABLE = "error.409.eventNotEditable";
    public static final String EVENT_NOT_ERASABLE = "error.409.eventNotErasable";
    public static final String EVENT_NOT_ERASABLE_APPROVED = "error.409.eventNotErasableAppr";
    public static final String AGENDA_EVENT_NOT_AVAILABLE = "error.409.agendaEventNotAvailable";
    public static final String PROMOTION_NOT_ELIGIBLE = "error.409.promotionNotEligible";
    public static final String PROMOTION_ALREADY_REQUESTED = "error.409.promotionAlreadyRequested";
    public static final String ACCOUNT_PENDING_DELETION = "error.409.accountPendingDeletion";
    public static final String ACCOUNT_NOT_PENDING_DELETION = "error.409.accountNotPendingDeletion";

    // Conflict 409 genérico (constraint de BD no capturada explícitamente)
    public static final String DB_CONSTRAINT = "error.409.dbConstraint";

    // Too Many Requests 429
    public static final String RATE_LIMIT_EXCEEDED = "error.429.rateLimitExceeded";

    // Unprocessable Entity 422
    public static final String TYPE_422_STORAGE = "storage-error";
    public static final String INVALID_IMAGE = "error.422.invalidImage";
    public static final String STORAGE_UPLOAD_FAILED = "error.422.storageUploadFailed";
    public static final String SCRAPE_INVALID_URL = "error.422.scrapeInvalidUrl";
    public static final String SCRAPE_UNREACHABLE = "error.422.scrapeUnreachable";
    public static final String SCRAPE_NO_OG_IMAGE = "error.422.scrapeNoOgImage";
    public static final String SCRAPE_FACEBOOK_BLOCKED = "error.422.scrapeFacebookBlocked";

    // Server Error 500
    public static final String SERVER = "error.500.server";

    /*
     * Titles
     */
    public static final String TIT_VALIDATION_ERROR = "Validation error";
    public static final String TIT_STORAGE_ERROR = "Storage error";
}
