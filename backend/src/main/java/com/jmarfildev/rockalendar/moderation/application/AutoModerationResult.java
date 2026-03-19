package com.jmarfildev.rockalendar.moderation.application;

import java.util.UUID;

import com.jmarfildev.rockalendar.moderation.domain.ModerationRuleType;

/**
 * Resultado de la evaluación de moderación automática.
 * flagged=false → el evento pasa a moderación humana (PENDING_MODERATION).
 * flagged=true  → el evento debe marcarse como FLAGGED.
 *
 * @author jmarfil
 */
public record AutoModerationResult(boolean flagged, ModerationRuleType ruleType, UUID ruleId, String matchedValue) {

    public static AutoModerationResult pass() {
        return new AutoModerationResult(false, null, null, null);
    }

    public static AutoModerationResult flag(ModerationRuleType ruleType, UUID ruleId, String matchedValue) {
        return new AutoModerationResult(true, ruleType, ruleId, matchedValue);
    }
}
