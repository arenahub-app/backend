package com.arenahub.infrastructure.security;

import com.arenahub.domain.user.User;
import com.arenahub.domain.user.vo.Email;
import com.arenahub.domain.user.vo.Name;
import com.arenahub.domain.user.vo.Phone;
import com.arenahub.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
                15, 7
        );
        jwtService = new JwtService(props);
    }

    @Test
    void generateAccessToken_isValidAndContainsUserId() {
        User user = User.registerLocal(
                new Name("João"), new Email("joao@example.com"), "hash",
                new Phone("11999999999"), LocalDate.of(1990, 1, 1));

        String token = jwtService.generateAccessToken(user);

        Optional<UUID> userId = jwtService.extractUserId(token);
        assertThat(userId).isPresent().contains(user.getId());
    }

    @Test
    void generatePurposeToken_isNotExtractableAsUserId() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generatePurposeToken(userId, "email-verification", Duration.ofHours(1));

        assertThat(jwtService.extractUserId(token)).isEmpty();
    }

    @Test
    void parseClaims_returnsPurposeClaim() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generatePurposeToken(userId, "password-reset", Duration.ofHours(1));

        Optional<Claims> claims = jwtService.parseClaims(token);
        assertThat(claims).isPresent();
        assertThat(claims.get().get("purpose")).isEqualTo("password-reset");
    }

    @Test
    void parseClaims_returnsEmpty_forInvalidToken() {
        Optional<Claims> claims = jwtService.parseClaims("invalid.token.here");
        assertThat(claims).isEmpty();
    }

    @Test
    void extractUserId_returnsEmpty_forExpiredToken() throws InterruptedException {
        User user = User.registerLocal(
                new Name("João"), new Email("joao@example.com"), "hash",
                new Phone("11999999999"), LocalDate.of(1990, 1, 1));

        JwtProperties shortProps = new JwtProperties(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
                0, 7
        );
        JwtService shortService = new JwtService(shortProps);
        String token = shortService.generateAccessToken(user);

        Thread.sleep(1100);

        assertThat(shortService.extractUserId(token)).isEmpty();
    }
}
