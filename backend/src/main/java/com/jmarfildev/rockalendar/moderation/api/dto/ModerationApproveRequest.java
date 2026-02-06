package com.jmarfildev.rockalendar.moderation.api.dto;

import jakarta.validation.constraints.Size;

/**
 * @author jmarfil
 *
 */
public record ModerationApproveRequest(@Size(max = 500) String comment) {}
