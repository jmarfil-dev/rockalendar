package com.jmarfildev.rockalendar.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * @author jmarfil
 */
public record ForgotPasswordRequest(@NotBlank @Email String email) {}
