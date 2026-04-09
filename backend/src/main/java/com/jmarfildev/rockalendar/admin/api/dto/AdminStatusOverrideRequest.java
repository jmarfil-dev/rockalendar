package com.jmarfildev.rockalendar.admin.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 */
public record AdminStatusOverrideRequest(@NotNull EventStatus targetStatus, @Size(max = 500) String reason) {}
