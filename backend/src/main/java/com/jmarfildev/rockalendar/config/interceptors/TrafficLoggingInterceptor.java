package com.jmarfildev.rockalendar.config.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor temporal para trazabilidad básica de tráfico público.
 * Eliminar cuando haya una solución de observabilidad real en su lugar.
 */
@Slf4j
@Component
public class TrafficLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String path = query != null ? uri + "?" + query : uri;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = auth == null || auth instanceof AnonymousAuthenticationToken;
        String principal = isAnonymous ? "anon" : "user";

        String ip = resolveClientIp(request);

        log.info("[{}] {} — {} — {}", method, path, principal, ip);

        return true;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For puede contener varios IPs separados por comas; el primero es el cliente real
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
