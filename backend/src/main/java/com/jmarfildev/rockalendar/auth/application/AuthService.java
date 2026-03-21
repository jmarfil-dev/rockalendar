package com.jmarfildev.rockalendar.auth.application;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.auth.api.dto.AuthTokenResponse;
import com.jmarfildev.rockalendar.auth.api.dto.LoginRequest;
import com.jmarfildev.rockalendar.auth.api.dto.RegisterRequest;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.users.domain.User;
import com.jmarfildev.rockalendar.users.domain.UserRole;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * @author jmarfil
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthTokenResponse login(LoginRequest request) {
        Authentication auth;
        try {
            auth = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        }
        catch (AuthenticationException e) {
            throw new BadCredentialsException(ErrorConstants.INVALID_CREDENTIALS);
        }

        var user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new BadCredentialsException(ErrorConstants.INVALID_CREDENTIALS));

        var authorities = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();

        var token = jwtTokenService.createToken(user, authorities);
        return new AuthTokenResponse(token.token(), token.expiresAt());
    }

    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER.name());
        user.setPrivacyAccepted(true);

        try {
            userRepository.saveAndFlush(user); // flush para detectar unique de email aquí
        }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException(ErrorConstants.EMAIL_ALREADY_EXISTS);
        }

        var token = jwtTokenService.createToken(user, List.of(user.getRole()));
        log.info("user registered userId={}", user.getId());
        return new AuthTokenResponse(token.token(), token.expiresAt());
    }
}
