package com.jmarfildev.rockalendar.auth.api;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.auth.api.dto.AuthTokenResponse;
import com.jmarfildev.rockalendar.auth.api.dto.ForgotPasswordRequest;
import com.jmarfildev.rockalendar.auth.api.dto.LoginRequest;
import com.jmarfildev.rockalendar.auth.api.dto.RegisterRequest;
import com.jmarfildev.rockalendar.auth.api.dto.ResetPasswordRequest;
import com.jmarfildev.rockalendar.auth.application.AuthService;
import com.jmarfildev.rockalendar.auth.application.PasswordResetService;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @Override
    public AuthTokenResponse login(LoginRequest request) {
        return authService.login(request);
    }

    @Override
    public AuthTokenResponse register(RegisterRequest request) {
        return authService.register(request);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        passwordResetService.requestReset(request);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
    }
}
