package com.jmarfildev.rockalendar.common;

/**
 * @author jmarfil
 *
 */
public final class Constants {
    private Constants() {}

    // Pagination
    public static final int MAX_PAGE_SIZE = 100;

    // JWT claim names
    public static final String JWT_CLAIM_EMAIL = "email";
    public static final String JWT_CLAIM_ROLES = "roles";

    // HTTP headers de renovación de token
    public static final String HEADER_REFRESH_TOKEN = "X-Refresh-Token";
    public static final String HEADER_REFRESH_TOKEN_EXPIRES_AT = "X-Refresh-Token-Expires-At";

    // Imagen / almacenamiento
    public static final String IMAGE_CONTENT_TYPE = "image/jpeg";
    public static final String IMAGE_CONTENT_TYPE_PREFIX = "image/";
    public static final String IMAGE_OUTPUT_FORMAT = "jpeg";
}
