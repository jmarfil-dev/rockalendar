package com.jmarfildev.rockalendar.users.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.users.api.dto.MeDto;
import com.jmarfildev.rockalendar.users.api.mapper.UserMapper;
import com.jmarfildev.rockalendar.users.domain.User;
import com.jmarfildev.rockalendar.users.domain.UserRole;
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

    @Transactional
    public MeDto requestPromotion() {
        var userId = currentUser.userId();

        if (!eligibilityService.isEligible(userId)) {
            throw new ConflictException(ErrorConstants.PROMOTION_NOT_ELIGIBLE);
        }

        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(UserRole.MODERATOR.name());
        log.info("user promoted to MODERATOR userId={}", userId);

        // El usuario ya es MODERATOR: promotionEligible es siempre false
        return userMapper.toMeDto(user, false);
    }
}
