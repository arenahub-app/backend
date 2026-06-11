package com.arenahub.domain.user;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PasswordResetTokenTest {

    @Test
    void issue_setsCorrectFields() {
        UUID userId = UUID.randomUUID();
        PasswordResetToken prt = PasswordResetToken.issue(userId, "hash123", Duration.ofHours(1));

        assertThat(prt.getId()).isNotNull();
        assertThat(prt.getUserId()).isEqualTo(userId);
        assertThat(prt.getTokenHash()).isEqualTo("hash123");
        assertThat(prt.getExpiresAt()).isAfter(Instant.now());
        assertThat(prt.getUsedAt()).isNull();
        assertThat(prt.getCreatedAt()).isNotNull();
    }

    @Test
    void isValid_returnsTrue_forFreshToken() {
        PasswordResetToken prt = PasswordResetToken.issue(UUID.randomUUID(), "hash", Duration.ofHours(1));

        assertThat(prt.isValid()).isTrue();
    }

    @Test
    void isValid_returnsFalse_whenExpired() throws InterruptedException {
        PasswordResetToken prt = PasswordResetToken.issue(UUID.randomUUID(), "hash", Duration.ofMillis(1));
        Thread.sleep(5);

        assertThat(prt.isValid()).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenAlreadyUsed() {
        PasswordResetToken prt = PasswordResetToken.issue(UUID.randomUUID(), "hash", Duration.ofHours(1));
        prt.markAsUsed();

        assertThat(prt.isValid()).isFalse();
    }

    @Test
    void markAsUsed_setsUsedAt() {
        PasswordResetToken prt = PasswordResetToken.issue(UUID.randomUUID(), "hash", Duration.ofHours(1));
        Instant before = Instant.now();

        prt.markAsUsed();

        assertThat(prt.getUsedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void reconstitute_setsAllFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(3600);

        PasswordResetToken prt = PasswordResetToken.reconstitute(id, userId, "hash", expiry, now, now);

        assertThat(prt.getId()).isEqualTo(id);
        assertThat(prt.getUserId()).isEqualTo(userId);
        assertThat(prt.getTokenHash()).isEqualTo("hash");
        assertThat(prt.getExpiresAt()).isEqualTo(expiry);
        assertThat(prt.isValid()).isFalse(); // usedAt is set
    }
}
