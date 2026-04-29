package com.jmarfildev.rockalendar.users.application;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.notifications.application.NotificationService;
import com.jmarfildev.rockalendar.notifications.domain.NotificationType;
import com.jmarfildev.rockalendar.users.api.dto.ChangeLocaleRequest;
import com.jmarfildev.rockalendar.users.api.dto.ChangePasswordRequest;
import com.jmarfildev.rockalendar.users.api.dto.MeDto;
import com.jmarfildev.rockalendar.users.api.mapper.UserMapper;
import com.jmarfildev.rockalendar.users.domain.User;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MeCommandService {

    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final PromotionEligibilityService eligibilityService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Transactional
    public MeDto requestPromotion() {
        var userId = currentUser.userId();
        User user = userRepository.findById(userId).orElseThrow();

        if (user.getPromotionRequestedAt() != null) {
            throw new ConflictException(ErrorConstants.PROMOTION_ALREADY_REQUESTED);
        }

        if (!eligibilityService.isEligible(userId)) {
            throw new ConflictException(ErrorConstants.PROMOTION_NOT_ELIGIBLE);
        }

        user.setPromotionRequestedAt(OffsetDateTime.now());
        log.info("promotion request submitted userId={}", userId);

        notificationService.notifyAllAdmins(
                NotificationType.PROMOTION_REQUEST,
                null,
                Map.of("email", user.getEmail())
        );

        return userMapper.toMeDto(user, false);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        var userId = currentUser.userId();
        User user = userRepository.findById(userId).orElseThrow();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException(ErrorConstants.WRONG_CURRENT_PASSWORD);
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException(ErrorConstants.WRONG_CONFIRM_PASSWORD);
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        log.info("password changed userId={}", userId);
    }

    @Transactional
    public void requestDeletion() {
        var userId = currentUser.userId();
        User user = userRepository.findById(userId).orElseThrow();

        if (user.getDeletionRequestedAt() != null) {
            throw new ConflictException(ErrorConstants.ACCOUNT_PENDING_DELETION);
        }

        user.setDeletionRequestedAt(OffsetDateTime.now());
        log.info("account deletion requested userId={}", userId);
    }

    @Transactional
    public void changeLocale(ChangeLocaleRequest request) {
        var userId = currentUser.userId();
        User user = userRepository.findById(userId).orElseThrow();
        user.setPreferredLanguage(request.locale());
        log.info("locale changed userId={} locale={}", userId, request.locale());
    }

    @Transactional
    public void cancelDeletion() {
        var userId = currentUser.userId();
        User user = userRepository.findById(userId).orElseThrow();

        if (user.getDeletionRequestedAt() == null) {
            throw new ConflictException(ErrorConstants.ACCOUNT_NOT_PENDING_DELETION);
        }

        user.setDeletionRequestedAt(null);
        log.info("account deletion cancelled userId={}", userId);
    }
}
