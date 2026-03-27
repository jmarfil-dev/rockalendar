package com.jmarfildev.rockalendar.moderation.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.artists.domain.Artist;
import com.jmarfildev.rockalendar.common.helper.SlugNormalizer;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.moderation.domain.AutoModerationLog;
import com.jmarfildev.rockalendar.moderation.domain.ModerationRule;
import com.jmarfildev.rockalendar.moderation.domain.ModerationRuleType;
import com.jmarfildev.rockalendar.moderation.persistence.AutoModerationLogRepository;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationConfigRepository;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationRuleRepository;

/**
 * Servicio de moderación automática.
 * Evalúa las reglas de blacklist y los parámetros de configuración para decidir
 * si un evento debe ser marcado como FLAGGED antes de pasar a moderación humana.
 *
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutoModerationService {

    private final ModerationRuleRepository ruleRepository;
    private final ModerationConfigRepository configRepository;
    private final AutoModerationLogRepository logRepository;
    private final EventRepository eventRepository;

    /**
     * Evalúa si el evento debe ser marcado automáticamente.
     * El orden de comprobación es: anti-spam → blacklist.
     *
     * @param title       título del evento
     * @param description descripción del evento (puede ser null)
     * @param artists     artistas del evento
     * @param userId      id del usuario proponente
     * @return PASS si el evento puede pasar a moderación humana, FLAG con el motivo en caso contrario
     */
    public AutoModerationResult evaluate(String title, String description, Set<Artist> artists, UUID userId) {
        int spamCount = getConfigInt("spam_rejection_count", 5);
        int spamWindowDays = getConfigInt("spam_window_days", 30);

        // Comprobar anti-spam
        OffsetDateTime spamSince = OffsetDateTime.now().minusDays(spamWindowDays);
        long recentRejections = eventRepository.countRejectedByUserSince(userId, spamSince);
        if (recentRejections >= spamCount) {
            log.info("auto-moderation SPAM userId={} recentRejections={}", userId, recentRejections);
            return AutoModerationResult.flag(ModerationRuleType.SPAM, null, String.valueOf(recentRejections));
        }

        // Comprobar reglas de blacklist
        List<ModerationRule> rules = ruleRepository.findAllByActiveTrue();
        String content = SlugNormalizer.removeAccents(title + " " + (description != null ? description : "")).toLowerCase();

        for (ModerationRule rule : rules) {
            AutoModerationResult result = switch (rule.getRuleType()) {
                case TEXT_TERM -> evaluateTextTerm(rule, content);
                case REGEX -> evaluateRegex(rule, content);
                case ARTIST_SLUG -> evaluateArtistSlug(rule, artists);
                default -> null; // TRUST_SCORE y SPAM no se evalúan con reglas de BD
            };
            if (result != null && result.flagged()) {
                return result;
            }
        }

        return AutoModerationResult.pass();
    }

    private AutoModerationResult evaluateTextTerm(ModerationRule rule, String content) {
        if (!content.contains(rule.getValue().toLowerCase())) {
            return AutoModerationResult.pass();
        }
        log.info("auto-moderation TEXT_TERM ruleId={} value={}", rule.getId(), rule.getValue());
        return AutoModerationResult.flag(ModerationRuleType.TEXT_TERM, rule.getId(), rule.getValue());
    }

    private AutoModerationResult evaluateRegex(ModerationRule rule, String content) {
        try {
            Pattern pattern = Pattern.compile(rule.getValue(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            if (!pattern.matcher(content).find()) {
                return AutoModerationResult.pass();
            }
            log.info("auto-moderation REGEX ruleId={} pattern={}", rule.getId(), rule.getValue());
            return AutoModerationResult.flag(ModerationRuleType.REGEX, rule.getId(), rule.getValue());
        }
        catch (PatternSyntaxException e) {
            log.warn("auto-moderation invalid regex ruleId={} pattern={}", rule.getId(), rule.getValue());
            return AutoModerationResult.pass();
        }
    }

    private AutoModerationResult evaluateArtistSlug(ModerationRule rule, Set<Artist> artists) {
        return artists.stream().filter(a -> a.getSlug().equals(rule.getValue())).findFirst().map(a -> {
            log.info("auto-moderation ARTIST_SLUG ruleId={} slug={}", rule.getId(), a.getSlug());
            return AutoModerationResult.flag(ModerationRuleType.ARTIST_SLUG, rule.getId(), a.getSlug());
        }).orElse(AutoModerationResult.pass());
    }

    /**
     * Persiste la entrada de log cuando un evento es marcado automáticamente.
     * Debe invocarse dentro de la transacción del servicio que guarda el evento.
     */
    @Transactional
    public void logFlag(UUID eventId, AutoModerationResult result) {
        var entry = AutoModerationLog.builder()
                                     .eventId(eventId)
                                     .ruleId(result.ruleId())
                                     .ruleType(result.ruleType())
                                     .matchedValue(result.matchedValue())
                                     .build();
        logRepository.save(entry);
        log.info("auto-moderation flag logged eventId={} ruleType={}", eventId, result.ruleType());
    }

    private int getConfigInt(String key, int defaultValue) {
        return configRepository.findByKey(key).map(c -> {
            try {
                return Integer.parseInt(c.getValue());
            }
            catch (NumberFormatException e) {
                log.warn("moderation config invalid int key={} value={}", key, c.getValue());
                return defaultValue;
            }
        }).orElse(defaultValue);
    }
}
