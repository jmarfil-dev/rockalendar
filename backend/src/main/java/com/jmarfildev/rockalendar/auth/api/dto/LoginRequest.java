package com.jmarfildev.rockalendar.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @author jmarfil
 *
 */
public record LoginRequest(@NotBlank String email,
                           @NotBlank String password) {}
