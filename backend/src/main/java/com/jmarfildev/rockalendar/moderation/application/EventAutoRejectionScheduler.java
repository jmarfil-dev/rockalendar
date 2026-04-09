package com.jmarfildev.rockalendar.moderation.application;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.moderation.domain.ActionType;
import com.jmarfildev.rockalendar.moderation.domain.ModerationAction;
import com.jmarfildev.rockalendar.moderation.persistence.AutoModerationLogRepository;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationActionRepository;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationConfigRepository;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationRuleRepository;
import com.jmarfildev.rockalendar.users.application.TrustScoreService;

/**
 * Scheduler que rechaza automáticamente eventos en estados intermedios cuando superan
 * el tiempo de espera configurado:
 * <ul>
 *   <li>FLAGGED → rechazados tras {@code flagged_rejection_delay_hours} (default 24 h).
 *       Se registra AUTO_REJECT y se comprueba autoban.</li>
 *   <li>NEEDS_CHANGES → rechazados tras {@code needs_changes_rejection_delay_hours} (default 360 h = 15 días).
 *       Se registra STALE_REJECT (peso 0) sin penalización de trust score.</li>
 * </ul>
 *
 * @author jmarfil
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventAutoRejectionScheduler {

    private static final String FALLBACK_REJECTION_MESSAGE = "El evento no cumple los requisitos de publicación.";
    private static final String STALE_REJECTION_MESSAGE =
            "El evento fue rechazado automáticamente por no recibir los cambios solicitados en el plazo establecido.";
    private static final int DEFAULT_FLAGGED_DELAY_HOURS = 24;
    private static final int DEFAULT_STALE_DELAY_HOURS = 360; // 15 días

    private final EventRepository eventRepository;
    private final ModerationConfigRepository configRepository;
    private final AutoModerationLogRepository autoModerationLogRepository;
    private final ModerationRuleRepository ruleRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final TrustScoreService trustScoreService;

    @Scheduled(fixedDelay = 21_600_000) // cada 6 horas
    @Transactional
    public void rejectFlaggedEvents() {
        int delayHours = getConfigInt("flagged_rejection_delay_hours", DEFAULT_FLAGGED_DELAY_HOURS);
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(delayHours);

        List<Event> flaggedEvents = eventRepository.findByStatusAndUpdatedAtBefore(EventStatus.FLAGGED, threshold);

        if (flaggedEvents.isEmpty()) {
            return;
        }

        log.info("auto-rejection scheduler: procesando {} eventos FLAGGED", flaggedEvents.size());

        OffsetDateTime now = OffsetDateTime.now();
        for (Event event : flaggedEvents) {
            String message =
                    autoModerationLogRepository.findByEventId(event.getId())
                                               .flatMap(logEntry -> logEntry.getRuleId() != null
                                                       ? ruleRepository.findById(logEntry.getRuleId()).map(rule -> rule.getReason())
                                                       : java.util.Optional.empty())
                                               .orElse(FALLBACK_REJECTION_MESSAGE);

            event.setStatus(EventStatus.REJECTED);
            event.setModerationMessage(message);
            event.setModeratedAt(now);
            // moderatedByUserId queda null — indica acción del sistema

            ModerationAction action = new ModerationAction();
            action.setEventId(event.getId());
            action.setAction(ActionType.AUTO_REJECT);
            action.setReason(message);
            action.setCreatedAt(now);
            moderationActionRepository.saveAndFlush(action);

            trustScoreService.checkAutoban(event.getCreatedByUserId());
            log.info("auto-rejected FLAGGED eventId={}", event.getId());
        }
    }

    @Scheduled(fixedDelay = 86_400_000) // cada 24 horas
    @Transactional
    public void rejectStaleNeedsChangesEvents() {
        int delayHours = getConfigInt("needs_changes_rejection_delay_hours", DEFAULT_STALE_DELAY_HOURS);
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(delayHours);

        List<Event> staleEvents = eventRepository.findByStatusAndUpdatedAtBefore(EventStatus.NEEDS_CHANGES, threshold);

        if (staleEvents.isEmpty()) {
            return;
        }

        log.info("stale-rejection scheduler: procesando {} eventos NEEDS_CHANGES", staleEvents.size());

        OffsetDateTime now = OffsetDateTime.now();
        for (Event event : staleEvents) {
            event.setStatus(EventStatus.REJECTED);
            event.setModerationMessage(STALE_REJECTION_MESSAGE);
            event.setModeratedAt(now);
            // moderatedByUserId queda null — indica acción del sistema

            ModerationAction action = new ModerationAction();
            action.setEventId(event.getId());
            action.setAction(ActionType.STALE_REJECT);
            action.setReason(STALE_REJECTION_MESSAGE);
            action.setCreatedAt(now);
            moderationActionRepository.save(action);

            log.info("stale-rejected NEEDS_CHANGES eventId={}", event.getId());
        }
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
