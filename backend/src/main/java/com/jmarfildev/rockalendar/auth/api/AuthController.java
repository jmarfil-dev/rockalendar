package com.jmarfildev.rockalendar.auth.api;

import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.auth.api.dto.AuthTokenResponse;
import com.jmarfildev.rockalendar.auth.api.dto.ForgotPasswordRequest;
import com.jmarfildev.rockalendar.auth.api.dto.LoginRequest;
import com.jmarfildev.rockalendar.auth.api.dto.RegisterRequest;
import com.jmarfildev.rockalendar.auth.api.dto.ResetPasswordRequest;
import com.jmarfildev.rockalendar.auth.application.AuthService;
import com.jmarfildev.rockalendar.auth.application.PasswordResetService;
import com.jmarfildev.rockalendar.common.helper.StringUtils;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthApi {

    // Token fake que parece legítimo para no delatar la trampa al bot
    private static final String HONEYPOT_FAKE_TOK = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cmFwIn0.honeypot";

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @Override
    public AuthTokenResponse login(LoginRequest request) {
        return authService.login(request);
    }

    @Override
    public AuthTokenResponse register(RegisterRequest request) {
        if (StringUtils.blankToNull(request.website()) != null) {
            log.warn("Honeypot activado en /register (website='{}')", request.website());
            return new AuthTokenResponse(HONEYPOT_FAKE_TOK, Instant.now().plusSeconds(3600));
        }
        return authService.register(request);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        if (StringUtils.blankToNull(request.website()) != null) {
            log.warn("Honeypot activado en /forgot-password (website='{}')", request.website());
            return;
        }
        passwordResetService.requestReset(request);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
    }
}
