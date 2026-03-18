package com.jmarfildev.rockalendar.users.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.moderation.domain.ActionType;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationActionRepository;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * Actualiza el trust score del usuario propietario de un evento tras una acción de moderación.
 * Debe invocarse dentro de una transacción activa (la del servicio de moderación).
 *
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrustScoreService {

    public static final int DELTA_APPROVED_DIRECT = 10;
    public static final int DELTA_APPROVED_AFTER_CHANGES = 5;
    public static final int DELTA_REJECTED = -15;
    public static final int DELTA_REJECTED_AFTER_MANY_CHANGES = -30;
    public static final int BAN_THRESHOLD = -200;
    public static final int REQUEST_CHANGES_THRESHOLD = 3;

    private final UserRepository userRepository;
    private final ModerationActionRepository moderationActionRepository;

    /**
     * Aplica el delta de trust score tras una aprobación.
     * +10 si no hubo solicitudes de cambios previas, +5 si las hubo.
     */
    public void onApprove(UUID userId, UUID eventId) {
        long priorChanges = moderationActionRepository.countByEventIdAndAction(eventId, ActionType.REQUEST_CHANGES);
        int delta = priorChanges > 0 ? DELTA_APPROVED_AFTER_CHANGES : DELTA_APPROVED_DIRECT;
        applyDelta(userId, eventId, delta);
    }

    /**
     * Aplica el delta de trust score tras un rechazo.
     * -30 si hubo 3+ solicitudes de cambios previas, -15 en caso contrario.
     */
    public void onReject(UUID userId, UUID eventId) {
        long priorChanges = moderationActionRepository.countByEventIdAndAction(eventId, ActionType.REQUEST_CHANGES);
        int delta = priorChanges >= REQUEST_CHANGES_THRESHOLD ? DELTA_REJECTED_AFTER_MANY_CHANGES : DELTA_REJECTED;
        applyDelta(userId, eventId, delta);
    }

    /**
     * Aplica la penalización máxima (-30) cuando el rechazo se produce automáticamente
     * al detectar la tercera solicitud de cambios. Debe usarse en lugar de onReject()
     * para este caso concreto, ya que en ese momento solo hay 2 REQUEST_CHANGES en BD.
     */
    public void onRejectAfterThirdChange(UUID userId, UUID eventId) {
        applyDelta(userId, eventId, DELTA_REJECTED_AFTER_MANY_CHANGES);
    }

    private void applyDelta(UUID userId, UUID eventId, int delta) {
        userRepository.findById(userId).ifPresent(user -> {
            int newScore = user.getTrustScore() + delta;
            user.setTrustScore(newScore);
            if (newScore <= BAN_THRESHOLD) {
                user.setBanned(true);
                log.warn("trust score ban userId={} score={}", userId, newScore);
            }
            log.info("trust score delta={} newScore={} userId={} eventId={}", delta, newScore, userId, eventId);
        });
    }
}
