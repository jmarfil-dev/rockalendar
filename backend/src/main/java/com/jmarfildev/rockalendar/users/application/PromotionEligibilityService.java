package com.jmarfildev.rockalendar.users.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.users.domain.User;
import com.jmarfildev.rockalendar.users.domain.UserRole;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * Comprueba si un usuario cumple todos los requisitos para solicitar el ascenso a moderador.
 * La puntuación de confianza es interna y no se expone al usuario.
 *
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
public class PromotionEligibilityService {

    public static final int MIN_TRUST_SCORE = 100;
    public static final int MIN_SENIORITY_DAYS = 90;
    public static final int MAX_SAME_VENUE_RECENT = 3;
    public static final int MAX_SAME_ARTIST_RECENT = 3;
    public static final int MAX_SAME_VENUE_TOTAL = 10;
    public static final int MAX_SAME_ARTIST_TOTAL = 10;
    public static final int RECENT_PERIOD_DAYS = 30;

    private static final List<EventStatus> EXCLUDED_STATUSES = List.of(EventStatus.DRAFT, EventStatus.ERASED);

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public boolean isEligible(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        // Solo usuarios con rol USER pueden ascender
        if (user.roleEnum() != UserRole.USER) return false;

        // Ban permanente
        if (user.isBanned()) return false;

        // Trust score mínimo
        if (user.getTrustScore() < MIN_TRUST_SCORE) return false;

        // Antigüedad mínima
        if (user.getCreatedAt().isAfter(OffsetDateTime.now().minusDays(MIN_SENIORITY_DAYS))) return false;

        OffsetDateTime since = OffsetDateTime.now().minusDays(RECENT_PERIOD_DAYS);

        // Límite reciente por sala
        if (!eventRepository.findVenuesExceedingRecentLimit(userId, since, EXCLUDED_STATUSES, MAX_SAME_VENUE_RECENT).isEmpty()) {
            return false;
        }

        // Límite reciente por grupo
        if (!eventRepository.findArtistsExceedingRecentLimit(userId, since, EXCLUDED_STATUSES, MAX_SAME_ARTIST_RECENT).isEmpty()) {
            return false;
        }

        // Límite total por sala
        if (!eventRepository.findVenuesExceedingTotalLimit(userId, EXCLUDED_STATUSES, MAX_SAME_VENUE_TOTAL).isEmpty()) {
            return false;
        }

        // Límite total por grupo
        if (!eventRepository.findArtistsExceedingTotalLimit(userId, EXCLUDED_STATUSES, MAX_SAME_ARTIST_TOTAL).isEmpty()) {
            return false;
        }

        return true;
    }
}
