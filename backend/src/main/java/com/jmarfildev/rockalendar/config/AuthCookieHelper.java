package com.jmarfildev.rockalendar.config;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author jmarfil
 *
 */
@Component
public class AuthCookieHelper {

    public static final String COOKIE_NAME = "access_token";

    @Value("${security.cookie.secure:true}")
    private boolean secure;

    public ResponseCookie buildAuthCookie(String token, Instant expiresAt) {
        return ResponseCookie.from(COOKIE_NAME, token)
                             .httpOnly(true)
                             .secure(secure)
                             .path("/")
                             .sameSite("Strict")
                             .maxAge(Duration.between(Instant.now(), expiresAt))
                             .build();
    }

    public ResponseCookie clearAuthCookie() {
        return ResponseCookie.from(COOKIE_NAME, "").httpOnly(true).secure(secure).path("/").sameSite("Strict").maxAge(0).build();
    }

    public String readToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
