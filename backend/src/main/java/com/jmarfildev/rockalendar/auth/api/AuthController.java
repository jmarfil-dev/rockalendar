package com.jmarfildev.rockalendar.auth.api;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletResponse;

import com.jmarfildev.rockalendar.auth.api.dto.AuthSessionResponse;
import com.jmarfildev.rockalendar.auth.api.dto.ForgotPasswordRequest;
import com.jmarfildev.rockalendar.auth.api.dto.LoginRequest;
import com.jmarfildev.rockalendar.auth.api.dto.RegisterRequest;
import com.jmarfildev.rockalendar.auth.api.dto.ResetPasswordRequest;
import com.jmarfildev.rockalendar.auth.application.AuthService;
import com.jmarfildev.rockalendar.auth.application.PasswordResetService;
import com.jmarfildev.rockalendar.common.helper.StringUtils;
import com.jmarfildev.rockalendar.config.AuthCookieHelper;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final AuthCookieHelper cookieHelper;

    @Override
    public AuthSessionResponse login(LoginRequest request, HttpServletResponse response) {
        var token = authService.login(request);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHelper.buildAuthCookie(token.token(), token.expiresAt()).toString());
        return new AuthSessionResponse(token.expiresAt());
    }

    @Override
    public AuthSessionResponse register(RegisterRequest request, HttpServletResponse response) {
        if (StringUtils.blankToNull(request.website()) != null) {
            log.warn("Honeypot activado en /register (website='{}')", request.website());
            return new AuthSessionResponse(java.time.Instant.now().plusSeconds(3600));
        }
        var token = authService.register(request);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHelper.buildAuthCookie(token.token(), token.expiresAt()).toString());
        return new AuthSessionResponse(token.expiresAt());
    }

    @Override
    public void logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHelper.clearAuthCookie().toString());
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
