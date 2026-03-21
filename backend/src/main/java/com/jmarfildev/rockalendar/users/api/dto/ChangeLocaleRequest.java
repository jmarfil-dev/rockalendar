package com.jmarfildev.rockalendar.users.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.jmarfildev.rockalendar.common.error.ErrorConstants;

/**
 * @author jmarfil
 */
public record ChangeLocaleRequest(@NotBlank @Pattern(regexp = "^(es|en)$", message = ErrorConstants.INVALID_LOCALE) String locale) {}
