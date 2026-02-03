package com.jmarfildev.rockalendar.config;

import java.net.URI;
import java.util.List;

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
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author jmarfil
 *
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())// Para APIs REST en dev
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, authException) -> {
                            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
                            pd.setTitle(HttpStatus.UNAUTHORIZED.getReasonPhrase());
                            pd.setDetail("Authentication is required to access this resource");
                            pd.setType(URI.create("urn:rockalendar:error:unauthorized"));
                            pd.setInstance(URI.create(request.getRequestURI()));

                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                            objectMapper.writeValue(response.getOutputStream(), pd);
                        })
                        // Token válido pero sin Rol adecuado o acceso denegado por configuración
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
                            pd.setTitle(HttpStatus.FORBIDDEN.getReasonPhrase());
                            pd.setDetail("You don't have permission to access this resource");
                            pd.setType(URI.create("urn:rockalendar:error:forbidden"));
                            pd.setInstance(URI.create(request.getRequestURI()));

                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                            objectMapper.writeValue(response.getOutputStream(), pd);
                        }))
                .authorizeHttpRequests(auth -> auth
                        // Swagger / OpenAPI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                        .permitAll()
                        // Auth
                        .requestMatchers("/api/auth/**")
                        .permitAll()
                        // Público (lectura)
                        .requestMatchers(HttpMethod.GET, "/api/events/**", "/api/artists/**")
                        .permitAll()

                        // Proponer evento y "mis eventos" requieren login
                        .requestMatchers(HttpMethod.POST, "/api/events")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/me/**")
                        .authenticated()

                        // Moderación y admin
                        .requestMatchers("/api/moderation/**")
                        .hasAnyRole("MODERATOR", "ADMIN")
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // El resto, autenticado (por ahora)
                        .anyRequest()
                        .authenticated())

                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
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
            var roles = jwt.getClaimAsStringList("roles");
            return roles == null
                    ? List.of()
                    : roles.stream().map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r)).toList();
        });
        return conv;
    }
}
