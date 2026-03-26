package com.jmarfildev.rockalendar.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author jmarfil
 *
 */
public record LoginRequest(@NotBlank @Email String email, @NotBlank @Size(max = 72) String password) {}
