package com.jmarfildev.rockalendar.support;

/**
 * @author jmarfil
 *
 */
public final class TestConstants {

    private TestConstants() {}

    // ine_code típicos
    public static final short INE_MADRID = 28;
    public static final short INE_BARCELONA = 8;
    public static final short INE_SEVILLA = 41;
    public static final short INE_VALENCIA = 46;

    // Coinciden con un usuario del script R__seed_test_users
    public static final String MOCK_USER_ID = "aaaaaaaa-0000-0000-0000-000000000003";
    public static final String MOCK_EMAIL = "user@rockalendar.local";

    // Coinciden con un usuario del script R__seed_test_events
    public static final String MOCK_EVENT_ID_APPROVED = "cccccccc-0000-0000-0000-000000000001";

    public static final String MOCK_ARTIST_NAME_AY = "Against You";

    // Fechas típicas para tests
    public static final String MADRID_DATE = "2026-03-15T21:00:00Z";
    public static final String BARCELONA_DATE = "2026-04-02T21:00:00Z";
    public static final String GENERIC_DATE = "2026-06-20T21:30:00Z";
    public static final String RANGE_START_DATE = "2026-04-01T00:00:00Z";
    public static final String RANGE_END_DATE = "2026-04-30T23:59:59Z";

}
