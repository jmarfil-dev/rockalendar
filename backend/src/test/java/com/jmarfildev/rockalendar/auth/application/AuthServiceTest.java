package com.jmarfildev.rockalendar.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jmarfildev.rockalendar.auth.api.dto.AuthTokenResponse;
import com.jmarfildev.rockalendar.auth.api.dto.LoginRequest;
import com.jmarfildev.rockalendar.auth.api.dto.RegisterRequest;
import com.jmarfildev.rockalendar.auth.application.JwtTokenService.LoginToken;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.support.TestConstants;
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
    PasswordEncoder passwordEncoder;
    @Mock
    JwtTokenService jwtTokenService;

    @Test
    @DisplayName("login: credenciales válidas -> devuelve AuthTokenResponse")
    void login_validCredentials_returnsToken() {
        var req = new LoginRequest("User@Rockalendar.local", "pw");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                TestConstants.MOCK_EMAIL,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User user = User.builder()
                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .email(TestConstants.MOCK_EMAIL)
                .passwordHash("hash")
                .build();
        var token = new JwtTokenService.LoginToken("jwt-token", Instant.parse("2030-01-01T00:00:00Z"));
        var expected = new AuthTokenResponse(token.token(), token.expiresAt());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByEmail(TestConstants.MOCK_EMAIL))
                .thenReturn(Optional.of(user));
        when(jwtTokenService.createToken(eq(user), eq(List.of("ROLE_USER"))))
                .thenReturn(token);

        var result = service.login(req);

        assertThat(result).isEqualTo(expected);

        // Verifica que authenticate se llama con email/password del request
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo(TestConstants.MOCK_EMAIL);
        assertThat(captor.getValue().getCredentials()).isEqualTo("pw");
    }

    @Test
    @DisplayName("login: AuthenticationManager lanza AuthenticationException -> BadCredentialsException")
    void login_authenticationFails_throwsBadCredentials() {
        var req = new LoginRequest(TestConstants.MOCK_EMAIL, "wrong");

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
        var req = new LoginRequest(TestConstants.MOCK_EMAIL, "pw");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                TestConstants.MOCK_EMAIL,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByEmail(TestConstants.MOCK_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage(ErrorMessages.INVALID_CREDENTIALS);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    @DisplayName("register: email ya existe -> ConflictException")
    void register_throwsConflict_whenUniqueIndexIsViolated() {
        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint uk_users_email_lower"));

        var req = new RegisterRequest("User01@Test.com", "password123");

        assertThrows(ConflictException.class, () -> service.register(req));
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    @DisplayName("register: email se normaliza -> devuelve AuthTokenResponse")
    void register_savesUserNormalized_andReturnsToken() {
        var tokenObj = new LoginToken("jwt-token", Instant.parse("2030-01-01T00:00:00Z"));

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenService.createToken(any(User.class), anyList()))
                .thenReturn(tokenObj);

        var req = new RegisterRequest("User02@Test.com", "password123");

        var res = service.register(req);

        assertEquals("jwt-token", res.accessToken());
        assertEquals(Instant.parse("2030-01-01T00:00:00Z"), res.expiresAt());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());

        var saved = userCaptor.getValue();
        assertEquals("user02@test.com", saved.getEmail());
        assertEquals("hashed", saved.getPasswordHash());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> authCaptor = ArgumentCaptor.forClass(List.class);
        verify(jwtTokenService).createToken(eq(saved), authCaptor.capture());
        assertEquals(List.of(saved.getRole()), authCaptor.getValue());
    }
}
