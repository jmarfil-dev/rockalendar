package com.jmarfildev.rockalendar.auth.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmarfildev.rockalendar.auth.domain.PasswordResetToken;

/**
 * @author jmarfil
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    // Elimina todos los tokens previos del usuario al generar uno nuevo
    void deleteByUserId(UUID userId);
}
