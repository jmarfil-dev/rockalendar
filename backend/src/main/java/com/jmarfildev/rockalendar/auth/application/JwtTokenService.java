package com.jmarfildev.rockalendar.auth.application;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.users.domain.User;

/**
 * @author jmarfil
 *
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder encoder;
    @Value("${security.jwt.issuer}")
    private String issuer;
    @Value("${security.jwt.ttl-minutes}")
    private long ttlMinutes;

    public LoginToken createToken(User user, List<String> authorities) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlMinutes * 60);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(exp)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", authorities)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginToken(token, exp);
    }

    public record LoginToken(String token, Instant expiresAt) {}
}
