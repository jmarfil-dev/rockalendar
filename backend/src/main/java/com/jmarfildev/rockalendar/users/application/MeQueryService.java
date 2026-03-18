package com.jmarfildev.rockalendar.users.application;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.users.api.dto.MeDto;
import com.jmarfildev.rockalendar.users.api.mapper.UserMapper;
import com.jmarfildev.rockalendar.users.domain.User;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * @author jmarfil
 */
@Service
@RequiredArgsConstructor
public class MeQueryService {

    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final PromotionEligibilityService eligibilityService;
    private final UserMapper userMapper;

    public MeDto getMe() {
        var userId = currentUser.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorConstants.USER_NOT_FOUND));

        boolean eligible = eligibilityService.isEligible(userId);

        return userMapper.toMeDto(user, eligible);
    }
}
