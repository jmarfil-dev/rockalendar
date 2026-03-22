package com.jmarfildev.rockalendar.auth.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.auth.api.dto.ForgotPasswordRequest;
import com.jmarfildev.rockalendar.auth.api.dto.ResetPasswordRequest;
import com.jmarfildev.rockalendar.auth.domain.PasswordResetToken;
import com.jmarfildev.rockalendar.auth.persistence.PasswordResetTokenRepository;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * @author jmarfil
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_TTL_MINUTES = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${rockalendar.frontend.base-url}")
    private String frontendBaseUrl;

    /**
     * Siempre responde sin revelar si el email existe o no (previene enumeración de usuarios).
     */
    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();

        userRepository.findByEmail(email).ifPresent(user -> {
            // Invalidar tokens previos del mismo usuario
            tokenRepository.deleteByUserId(user.getId());
            tokenRepository.flush();

            String rawToken = generateRawToken();
            String tokenHash = hash(rawToken);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setTokenHash(tokenHash);
            resetToken.setExpiresAt(OffsetDateTime.now().plusMinutes(TOKEN_TTL_MINUTES));
            tokenRepository.save(resetToken);

            String resetLink = frontendBaseUrl + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(email, resetLink, user.getPreferredLanguage());
            log.info("password reset requested userId={}", user.getId());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hash(request.token());

        PasswordResetToken resetToken =
                tokenRepository.findByTokenHash(tokenHash).orElseThrow(() -> new BadRequestException(ErrorConstants.INVALID_RESET_TOKEN));

        if (!resetToken.isValid()) {
            throw new BadRequestException(ErrorConstants.INVALID_RESET_TOKEN);
        }

        resetToken.setUsedAt(OffsetDateTime.now());

        var user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        log.info("password reset completed userId={}", user.getId());
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encoded);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
