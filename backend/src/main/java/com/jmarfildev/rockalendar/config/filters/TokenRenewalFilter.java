package com.jmarfildev.rockalendar.config.filters;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.auth.application.JwtTokenService;
import com.jmarfildev.rockalendar.common.Constants;
import com.jmarfildev.rockalendar.users.domain.User;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * Renueva silenciosamente el JWT cuando le queda menos de la mitad de su vida útil.
 * Añade el nuevo token en las cabeceras X-Refresh-Token y X-Refresh-Token-Expires-At.
 * Se registra dentro de la cadena de Spring Security, tras BearerTokenAuthenticationFilter.
 *
 * @author jmarfil
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenRenewalFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    @Value("${security.jwt.ttl-minutes}")
    private long ttlMinutes;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Instant expiresAt = jwtAuth.getToken().getExpiresAt();

            if (expiresAt != null) {
                long remainingSeconds = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
                long thresholdSeconds = (ttlMinutes * 60) / 2;

                if (remainingSeconds < thresholdSeconds) {
                    String subject = jwtAuth.getToken().getSubject();
                    UUID userId = UUID.fromString(subject);

                    // Verificar que el usuario sigue activo antes de renovar
                    Optional<User> userOpt = userRepository.findById(userId);
                    if (userOpt.isEmpty() || userOpt.get().isErased()) {
                        // No renovar — dejar que el token expire naturalmente
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // Usar datos frescos de BD para el nuevo token
                    User user = userOpt.get();
                    List<String> freshRoles = List.of(user.roleEnum().asAuthority());
                    var newToken = jwtTokenService.renewToken(subject, user.getEmail(), freshRoles);
                    response.setHeader(Constants.HEADER_REFRESH_TOKEN, newToken.token());
                    response.setHeader(Constants.HEADER_REFRESH_TOKEN_EXPIRES_AT, newToken.expiresAt().toString());
                    log.debug("Token renovado para subject={} remainingSeconds={}", subject, remainingSeconds);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
