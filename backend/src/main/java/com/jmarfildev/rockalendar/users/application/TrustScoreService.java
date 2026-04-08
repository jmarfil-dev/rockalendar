package com.jmarfildev.rockalendar.users.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.moderation.persistence.ModerationActionRepository;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * Calcula el trust score de un usuario de forma derivada, sumando los pesos
 * de todas las acciones de moderación registradas sobre eventos que él creó.
 * El score nunca se persiste; se obtiene en tiempo real desde la BD.
 *
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrustScoreService {

    public static final int BAN_THRESHOLD = -200;

    private final UserRepository userRepository;
    private final ModerationActionRepository moderationActionRepository;

    /**
     * Devuelve el trust score derivado del usuario: suma de pesos de todas
     * sus acciones de moderación según la tabla action_weights.
     */
    public int getScore(UUID userId) {
        return moderationActionRepository.sumWeightsForUser(userId);
    }

    /**
     * Comprueba si el trust score del usuario ha alcanzado el umbral de ban.
     * Si es así, marca al usuario como baneado. Debe llamarse dentro de una
     * transacción activa, tras guardar la acción de moderación que puede hundir el score.
     */
    public void checkAutoban(UUID userId) {
        int score = getScore(userId);
        if (score <= BAN_THRESHOLD) {
            userRepository.findById(userId).ifPresent(user -> {
                if (!user.isBanned()) {
                    user.setBanned(true);
                    log.warn("trust score ban userId={} score={}", userId, score);
                }
            });
        }
    }
}
