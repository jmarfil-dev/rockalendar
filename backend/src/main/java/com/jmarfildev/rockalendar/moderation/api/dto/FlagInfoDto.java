package com.jmarfildev.rockalendar.moderation.api.dto;

import com.jmarfildev.rockalendar.moderation.domain.ModerationRuleType;

/**
 * Información de por qué un evento fue marcado por la auto-moderación.
 * Solo se incluye en la respuesta cuando el evento está en estado FLAGGED.
 *
 * @author jmarfil
 */
public record FlagInfoDto(ModerationRuleType ruleType, String reason, String matchedValue) {}
