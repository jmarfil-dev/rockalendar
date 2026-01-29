package com.jmarfildev.rockalendar.users.domain;

/**
 * @author jmarfil
 *
 */
public enum UserRole {
    USER,
    MODERATOR,
    ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }

    public static UserRole fromDb(String value) {
        return UserRole.valueOf(value.trim().toUpperCase());
    }
}
