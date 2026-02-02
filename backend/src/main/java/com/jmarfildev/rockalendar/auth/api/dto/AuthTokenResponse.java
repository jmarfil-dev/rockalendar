package com.jmarfildev.rockalendar.auth.api.dto;

import java.time.Instant;

/**
 * @author jmarfil
 *
 */
public record AuthTokenResponse(String accessToken, Instant expiresAt) {}
