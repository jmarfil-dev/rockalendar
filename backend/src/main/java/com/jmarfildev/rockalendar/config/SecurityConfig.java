package com.jmarfildev.rockalendar.config;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmarfildev.rockalendar.common.Constants;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.ProblemDetailGenericProperties;
import com.jmarfildev.rockalendar.config.filters.TokenRenewalFilter;

/**
 * @author jmarfil
 *
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final TokenRenewalFilter tokenRenewalFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true)
                                                                                .maxAgeInSeconds(31_536_000) // 1 año
                                                                                .preload(true)))
            .csrf(csrf -> csrf.disable())// Para APIs REST en dev
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(eh -> eh.authenticationEntryPoint(unauthorizedError())
                                       // Token válido pero sin Rol adecuado o acceso denegado por configuración
                                       .accessDeniedHandler(forbiddenError()))
            .authorizeHttpRequests(auth -> auth
                                               // Swagger / OpenAPI
                                               .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                                               .permitAll()
                                               // Auth
                                               .requestMatchers("/api/auth/**")
                                               .permitAll()
                                               // Contacto
                                               .requestMatchers(HttpMethod.POST, "/api/contact")
                                               .permitAll()
                                               // Público (lectura)
                                               .requestMatchers(HttpMethod.GET, "/api/events/**", "/api/artists/**", "/api/provinces/**")
                                               .permitAll()

                                               // Moderación y admin
                                               .requestMatchers("/api/moderation/**")
                                               .hasAnyRole("MODERATOR", "ADMIN")
                                               .requestMatchers("/api/admin/**")
                                               .hasRole("ADMIN")

                                               // El resto, autenticado (por ahora)
                                               .anyRequest()
                                               .authenticated())

            .oauth2ResourceServer(oauth2 -> oauth2.authenticationEntryPoint(unauthorizedError())
                                                  .accessDeniedHandler(forbiddenError())
                                                  .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .addFilterAfter(tokenRenewalFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    // Evita que Spring Boot registre TokenRenewalFilter también en el servlet container
    // (ya está registrado dentro de la cadena de Spring Security)
    @Bean
    FilterRegistrationBean<TokenRenewalFilter> tokenRenewalFilterRegistration() {
        var registration = new FilterRegistrationBean<>(tokenRenewalFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
            var roles = jwt.getClaimAsStringList(Constants.JWT_CLAIM_ROLES);
            return roles == null ? List.of() : roles.stream().map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r)).toList();
        });
        return conv;
    }

    private AuthenticationEntryPoint unauthorizedError() {
        return (request, response, authException) -> {
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
            ProblemDetailGenericProperties.setGenericProperties(pd, HttpStatus.UNAUTHORIZED.getReasonPhrase(), ErrorConstants.AUTH_REQUIRED,
                                                                request.getRequestURI(), ErrorConstants.TYPE_401_UNAUTHORIZED);

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), pd);
        };
    }

    private AccessDeniedHandler forbiddenError() {
        return (request, response, accessDeniedException) -> {
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
            ProblemDetailGenericProperties.setGenericProperties(pd, HttpStatus.FORBIDDEN.getReasonPhrase(), ErrorConstants.ACCESS_DENIED,
                                                                request.getRequestURI(), ErrorConstants.TYPE_403_FORBIDDEN);

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), pd);
        };
    }
}
