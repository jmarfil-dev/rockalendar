package com.jmarfildev.rockalendar.config.filters;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

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
                    String email = jwtAuth.getToken().getClaimAsString("email");
                    List<String> roles = jwtAuth.getAuthorities().stream().map(a -> a.getAuthority()).toList();

                    var newToken = jwtTokenService.renewToken(subject, email, roles);
                    response.setHeader("X-Refresh-Token", newToken.token());
                    response.setHeader("X-Refresh-Token-Expires-At", newToken.expiresAt().toString());
                    log.debug("Token renovado para subject={} remainingSeconds={}", subject, remainingSeconds);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
