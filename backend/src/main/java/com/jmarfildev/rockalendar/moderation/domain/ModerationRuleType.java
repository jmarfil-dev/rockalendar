package com.jmarfildev.rockalendar.moderation.domain;

/**
 * Tipo de regla de moderación automática.
 * TEXT_TERM, ARTIST_SLUG y REGEX son reglas de blacklist almacenadas en BD.
 * TRUST_SCORE y SPAM son reglas basadas en configuración, sin entrada en moderation_rules.
 *
 * @author jmarfil
 */
public enum ModerationRuleType {
    TEXT_TERM,
    ARTIST_SLUG,
    REGEX,
    TRUST_SCORE,
    SPAM
}
