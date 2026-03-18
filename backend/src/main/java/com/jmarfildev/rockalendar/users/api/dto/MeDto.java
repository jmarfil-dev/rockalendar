package com.jmarfildev.rockalendar.users.api.dto;

import java.util.UUID;

/**
 * @author jmarfil
 */
public record MeDto(
        UUID id,
        String email,
        String role,
        String preferredLanguage,
        boolean promotionEligible
) {}
