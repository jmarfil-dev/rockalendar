package com.jmarfildev.rockalendar.moderation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author jmarfil
 *
 */
public record ModerationArchiveRequest(@NotBlank @Size(max = 500) String reason) {}
