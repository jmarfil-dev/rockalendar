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
import com.jmarfildev.rockalendar.moderation.persistence.ModerationConfigRepository;

/**
 * Scheduler que rechaza automáticamente los eventos en estado FLAGGED
 * una vez transcurrido el tiempo de espera configurado.
 * Esto hace indistinguible para el usuario si fue rechazado por moderación humana o automática.
 *
 * @author jmarfil
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventAutoRejectionScheduler {

    private static final String REJECTION_MESSAGE = "El evento no cumple los requisitos de publicación.";
    private static final int DEFAULT_DELAY_HOURS = 24;

    private final EventRepository eventRepository;
    private final ModerationConfigRepository configRepository;

    @Scheduled(fixedDelay = 21_600_000) // cada 6 horas
    @Transactional
    public void rejectFlaggedEvents() {
        int delayHours = getConfigInt("flagged_rejection_delay_hours", DEFAULT_DELAY_HOURS);
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(delayHours);

        List<Event> flaggedEvents = eventRepository.findByStatusAndUpdatedAtBefore(EventStatus.FLAGGED, threshold);

        if (flaggedEvents.isEmpty()) {
            return;
        }

        log.info("auto-rejection scheduler: procesando {} eventos FLAGGED", flaggedEvents.size());

        for (Event event : flaggedEvents) {
            event.setStatus(EventStatus.REJECTED);
            event.setModerationMessage(REJECTION_MESSAGE);
            event.setModeratedAt(OffsetDateTime.now());
            // moderatedByUserId queda null — indica acción del sistema
            log.info("auto-rejected eventId={}", event.getId());
        }
    }

    private int getConfigInt(String key, int defaultValue) {
        return configRepository.findByKey(key)
                               .map(c -> {
                                   try {
                                       return Integer.parseInt(c.getValue());
                                   } catch (NumberFormatException e) {
                                       log.warn("moderation config invalid int key={} value={}", key, c.getValue());
                                       return defaultValue;
                                   }
                               })
                               .orElse(defaultValue);
    }
}
