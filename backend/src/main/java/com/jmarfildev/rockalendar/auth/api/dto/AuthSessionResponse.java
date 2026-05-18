package com.jmarfildev.rockalendar.auth.api.dto;

import java.time.Instant;

/**
 * @author jmarfil
 *
 */
public record AuthSessionResponse(Instant expiresAt) {}
