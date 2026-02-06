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
            return UUID.fromString(sub);
        }
        throw new IllegalStateException("Missing JWT subject");
    }
}
