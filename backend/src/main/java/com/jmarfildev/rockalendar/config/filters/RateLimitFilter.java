package com.jmarfildev.rockalendar.config.filters;

import java.io.IOException;
import java.time.Duration;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
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
 * Filtro de rate limiting para todos los endpoints de escritura (non-GET).
 * Aplica límites por IP+ruta normalizada usando Bucket4j con caché Caffeine.
 *
 * @author jmarfil
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${rockalendar.auth.rate-limit.path-prefix:/api/auth/}")
    private String authPathPrefix;

    @Value("${rockalendar.contact.rate-limit.path:/api/contact}")
    private String contactPath;

    private final ObjectMapper objectMapper;

    private static final Pattern UUID_PATTERN =
            Pattern.compile("[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);

    // Caché de buckets por clave "ip:rutaNormalizada" — expira tras 1h de inactividad
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder().expireAfterAccess(Duration.ofHours(1)).maximumSize(10_000).build();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Solo se aplica rate limiting a peticiones de escritura
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // Logout es una operación segura sin coste: limpiar una cookie
        if (request.getRequestURI().endsWith("/logout")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = extractIp(request);
        String path = request.getRequestURI();
        String normalizedPath = normalizePath(path);
        String bucketKey = ip + ":" + normalizedPath;

        Bucket bucket = buckets.get(bucketKey, k -> createBucketForPath(normalizedPath));

        var probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        }
        else {
            long retryAfterSeconds = (probe.getNanosToWaitForRefill() / 1_000_000_000L) + 1;
            log.warn("Rate limit superado: ip={} path={} retryAfter={}s", ip, normalizedPath, retryAfterSeconds);
            writeTooManyRequests(request, response, retryAfterSeconds);
        }
    }

    private String normalizePath(String path) {
        return UUID_PATTERN.matcher(path).replaceAll(":id");
    }

    private Bucket createBucketForPath(String path) {
        Bandwidth limit;
        if (path.endsWith("/login")) {
            // 10 intentos cada 10 minutos
            limit = Bandwidth.builder().capacity(10).refillIntervally(10, Duration.ofMinutes(10)).build();
        }
        else if (path.endsWith("/register") || path.equals(contactPath)) {
            // 5 intentos por hora
            limit = Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofHours(1)).build();
        }
        else if (path.startsWith(authPathPrefix)) {
            // forgot-password, reset-password: 2 intentos cada 10 minutos
            limit = Bandwidth.builder().capacity(2).refillIntervally(2, Duration.ofMinutes(10)).build();
        }
        else if (path.startsWith("/api/moderation/") || path.startsWith("/api/admin/")) {
            // Moderadores y admins procesando colas: límite generoso
            limit = Bandwidth.builder().capacity(300).refillIntervally(300, Duration.ofMinutes(10)).build();
        }
        else {
            // Escrituras de usuario estándar: proponer eventos, comentarios, etc.
            limit = Bandwidth.builder().capacity(60).refillIntervally(60, Duration.ofMinutes(10)).build();
        }
        return Bucket.builder().addLimit(limit).build();
    }

    private String extractIp(HttpServletRequest request) {
        // CF-Connecting-IP es inyectada por Cloudflare y no puede ser falsificada por el cliente
        String cfIp = request.getHeader("CF-Connecting-IP");
        if (cfIp != null && !cfIp.isBlank()) {
            return cfIp.strip();
        }
        // Fallback para entornos sin Cloudflare (local, staging directo)
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
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
