package com.jmarfildev.rockalendar.common.helper;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * @author jmarfil
 *
 */
@Component
public class CurrentUser {
    public UUID userId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            var sub = jwtAuth.getToken().getSubject();
            if (sub == null) {
                throw new IllegalStateException("JWT subject is null");
            }
            return UUID.fromString(sub);
        }
        throw new IllegalStateException("No JWT authentication in security context");
    }

    public boolean isAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                   .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
