package com.jmarfildev.rockalendar.users.application;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.users.domain.User;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * Anonimiza cuentas de usuario cuya solicitud de eliminación lleva más de 168 horas (7 días).
 * Se ejecuta cada 24 horas. Elimina los datos personales (email, contraseña) sin borrar el
 * registro de la base de datos, preservando la integridad referencial con eventos y acciones de moderación.
 *
 * @author jmarfil
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountDeletionScheduler {

    private static final long DELETION_GRACE_PERIOD_HOURS = 168L; // 7 días

    private final UserRepository userRepository;

    @Scheduled(fixedDelay = 86_400_000) // cada 24 horas
    @Transactional
    public void eraseRequestedAccounts() {
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(DELETION_GRACE_PERIOD_HOURS);
        List<User> pendingDeletion = userRepository.findByDeletionRequestedAtBeforeAndErasedFalse(threshold);

        if (pendingDeletion.isEmpty()) {
            return;
        }

        log.info("account-deletion scheduler: anonimizando {} cuentas", pendingDeletion.size());

        for (User user : pendingDeletion) {
            // Sustituir email por un valor único no recuperable para mantener la constraint UNIQUE
            String anonymizedEmail = "erased-" + user.getId() + "@rockalendar.invalid";
            user.setEmail(anonymizedEmail);
            user.setPasswordHash("");
            user.setPreferredLanguage(null);
            user.setDeletionRequestedAt(null);
            user.setErased(true);
            log.info("account erased userId={}", user.getId());
        }
    }
}
