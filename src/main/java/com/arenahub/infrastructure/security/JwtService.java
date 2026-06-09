package com.arenahub.infrastructure.security;

import com.arenahub.domain.user.User;
import com.arenahub.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.signingKey = io.jsonwebtoken.security.Keys
                .hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        long expirationMs = props.accessTokenExpirationMinutes() * 60_000L;
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail().value())
                .claim("profileIncomplete", user.hasIncompleteProfile())
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String generatePurposeToken(UUID userId, String purpose, Duration ttl) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("purpose", purpose)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttl.toMillis()))
                .signWith(signingKey)
                .compact();
    }

    public Optional<Claims> parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Optional<UUID> extractUserId(String token) {
        return parseClaims(token)
                .filter(c -> !c.containsKey("purpose"))
                .map(c -> UUID.fromString(c.getSubject()));
    }

    public long accessTokenExpirationSeconds() {
        return props.accessTokenExpirationMinutes() * 60L;
    }
}
