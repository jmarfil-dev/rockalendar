package com.jmarfildev.rockalendar.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.jmarfildev.rockalendar.auth.api.dto.LoginRequest;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.users.domain.User;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * @author jmarfil
 *
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    AuthService service;

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    UserRepository userRepository;
    @Mock
    JwtTokenService jwtTokenService;

    @Test
    @DisplayName("login: credenciales válidas -> devuelve LoginToken")
    void login_validCredentials_returnsToken() {
        var req = new LoginRequest("User@Test.com", "pw");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@test.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User user = User.builder()
                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .email("User@Test.com")
                .passwordHash("hash")
                .build();
        var expected = new JwtTokenService.LoginToken("jwt-token", Instant.parse("2030-01-01T00:00:00Z"));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByEmailIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(user));
        when(jwtTokenService.createToken(eq(user), eq(List.of("ROLE_USER"))))
                .thenReturn(expected);

        var result = service.login(req);

        assertThat(result).isEqualTo(expected);

        // Bonus: verifica que authenticate se llama con email/password del request
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("User@Test.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("pw");
    }

    @Test
    @DisplayName("login: AuthenticationManager lanza AuthenticationException -> BadCredentialsException")
    void login_authenticationFails_throwsBadCredentials() {
        var req = new LoginRequest("user@test.com", "wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("spring msg"));

        assertThatThrownBy(() -> service.login(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage(ErrorMessages.INVALID_CREDENTIALS);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    @DisplayName("login: auth ok pero usuario no existe -> BadCredentialsException")
    void login_userNotFound_throwsBadCredentials() {
        var req = new LoginRequest("user@test.com", "pw");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@test.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByEmailIgnoreCase("user@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage(ErrorMessages.INVALID_CREDENTIALS);
        verifyNoInteractions(jwtTokenService);
    }
}
