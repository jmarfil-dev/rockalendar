package com.jmarfildev.rockalendar.agenda.api.dto;

import jakarta.validation.constraints.NotNull;

import com.jmarfildev.rockalendar.agenda.domain.InteractionStatus;

/**
 * @author jmarfil
 */
public record SetInteractionRequest(@NotNull InteractionStatus status) {}
