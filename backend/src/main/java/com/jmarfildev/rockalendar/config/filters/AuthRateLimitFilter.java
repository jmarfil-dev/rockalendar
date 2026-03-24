package com.jmarfildev.rockalendar.config.filters;

import java.io.IOException;
import java.time.Duration;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.ProblemDetailGenericProperties;

/**
 * Filtro de rate limiting para endpoints de autenticación.
 * Aplica límites por IP usando Bucket4j con caché Caffeine.
 *
 * @author jmarfil
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final String AUTH_PATH_PREFIX = "/api/auth/";

    private final ObjectMapper objectMapper;

    // Caché de buckets por clave "ip:path" — expira tras 1h de inactividad
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder().expireAfterAccess(Duration.ofHours(1)).maximumSize(10_000).build();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(AUTH_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = extractIp(request);
        String path = request.getRequestURI();
        String bucketKey = ip + ":" + path;

        Bucket bucket = buckets.get(bucketKey, k -> createBucketForPath(path));

        var probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        }
        else {
            long retryAfterSeconds = (probe.getNanosToWaitForRefill() / 1_000_000_000L) + 1;
            log.warn("Rate limit superado: ip={} path={} retryAfter={}s", ip, path, retryAfterSeconds);
            writeTooManyRequests(request, response, retryAfterSeconds);
        }
    }

    private Bucket createBucketForPath(String path) {
        Bandwidth limit;
        if (path.endsWith("/login")) {
            // 10 intentos por minuto
            limit = Bandwidth.builder().capacity(10).refillIntervally(10, Duration.ofMinutes(10)).build();
        }
        else if (path.endsWith("/register")) {
            // 5 intentos por hora
            limit = Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofHours(1)).build();
        }
        else {
            // forgot-password, reset-password: 5 intentos por 10 minutos
            limit = Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofMinutes(10)).build();
        }
        return Bucket.builder().addLimit(limit).build();
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For puede contener una cadena de IPs; la primera es el cliente real
            return forwarded.split(",")[0].strip();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletRequest request, HttpServletResponse response, long retryAfterSeconds) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
        ProblemDetailGenericProperties.setGenericProperties(pd, "Too Many Requests", ErrorConstants.RATE_LIMIT_EXCEEDED,
                                                            request.getRequestURI(), ErrorConstants.TYPE_429_TOO_MANY_REQUESTS);
        pd.setProperty("retryAfter", retryAfterSeconds);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), pd);
    }
}
