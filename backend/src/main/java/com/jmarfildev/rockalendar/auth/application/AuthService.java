package com.jmarfildev.rockalendar.auth.application;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.auth.api.dto.LoginRequest;
import com.jmarfildev.rockalendar.auth.application.JwtTokenService.LoginToken;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * @author jmarfil
 *
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;

    public LoginToken login(LoginRequest request) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        }
        catch (AuthenticationException e) {
            throw new BadCredentialsException(ErrorMessages.INVALID_CREDENTIALS);
        }

        var user = userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new BadCredentialsException(ErrorMessages.INVALID_CREDENTIALS));

        var authorities = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();

        return jwtTokenService.createToken(user, authorities);
    }
}
