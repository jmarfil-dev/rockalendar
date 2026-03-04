package com.jmarfildev.rockalendar.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.jmarfildev.rockalendar.common.error.ErrorConstants;

/**
 * @author jmarfil
 *
 */
public record RegisterRequest(@NotBlank @Email String email,
                              @Size(min = 8,
                                    max = 72,
                                    message = ErrorConstants.VALID_SIZE_PASSWORD) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
                                                                message = ErrorConstants.VALID_PASSWORD) String password) {}
