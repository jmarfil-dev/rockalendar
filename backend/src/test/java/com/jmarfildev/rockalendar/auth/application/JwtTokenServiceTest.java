package com.jmarfildev.rockalendar.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.jmarfildev.rockalendar.config.SecurityJwtConfig;
import com.jmarfildev.rockalendar.users.domain.User;

/**
 * Usa @EnableAutoConfiguration en lugar de AbstractPostgresTest porque no necesita JPA ni DB.
 *
 * @author jmarfil
 *
 */
@SpringBootTest(
        classes = { JwtTokenService.class, SecurityJwtConfig.class },
        properties = {
                       "security.jwt.secret=01234567890123456789012345678901",
                       "security.jwt.issuer=urn:rockalendar:test",
                       "security.jwt.ttl-minutes=5"
        })
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
                                     HibernateJpaAutoConfiguration.class
})
class JwtTokenServiceTest {

    @Autowired
    JwtTokenService service;
    @Autowired
    JwtDecoder decoder;

    @Test
    @DisplayName("createToken: genera JWT válido con claims correctos")
    void createToken_generatesValidJwtWithClaims() {
        User user = User.builder()
                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .email("user@test.com")
                .build();

        var token = service.createToken(user, List.of("ROLE_USER"));
        Jwt jwt = decoder.decode(token.token());

        assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
        assertThat(jwt.getClaimAsString("email")).isEqualTo("user@test.com");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ROLE_USER");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("urn:rockalendar:test");
        assertThat(token.expiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("createToken: expiración respeta ttl-minutes")
    void createToken_expirationMatchesTtl() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@test.com")
                .build();
        var before = Instant.now();
        var token = service.createToken(user, List.of("ROLE_USER"));
        var after = Instant.now();

        Instant min = before.plusSeconds(5 * 60);
        Instant max = after.plusSeconds(5 * 60 + 2);

        assertThat(token.expiresAt())
                .isAfterOrEqualTo(min)
                .isBeforeOrEqualTo(max);
    }

    @Test
    @DisplayName("createToken: token manipulado -> decoder.decode lanza excepción")
    void createToken_tamperedToken_isRejected() {
        User user = User.builder()
                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .email("user@test.com")
                .passwordHash("x")
                .build();

        var loginToken = service.createToken(user, List.of("ROLE_USER"));
        String token = loginToken.token();

        // Se manipula el token: se cambia un carácter (sin romper el formato base64)
        String tamperedToken = token.substring(0, token.length() - 2) + "aa";

        assertThatThrownBy(() -> decoder.decode(tamperedToken)).isInstanceOf(JwtException.class);
    }
}
