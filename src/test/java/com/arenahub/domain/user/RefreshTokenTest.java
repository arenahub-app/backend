package com.arenahub.domain.user;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class RefreshTokenTest {

    @Test
    void issue_createsActiveToken() {
        RefreshToken rt = RefreshToken.issue(UUID.randomUUID(), "hash123", Duration.ofDays(7));

        assertThat(rt.getId()).isNotNull();
        assertThat(rt.getStatus()).isEqualTo(RefreshTokenStatus.ACTIVE);
        assertThat(rt.isValid()).isTrue();
    }

    @Test
    void revoke_makesTokenInvalid() {
        RefreshToken rt = RefreshToken.issue(UUID.randomUUID(), "hash123", Duration.ofDays(7));

        rt.revoke();

        assertThat(rt.getStatus()).isEqualTo(RefreshTokenStatus.REVOKED);
        assertThat(rt.isValid()).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenExpired() {
        RefreshToken rt = RefreshToken.issue(UUID.randomUUID(), "hash123", Duration.ofSeconds(-1));

        assertThat(rt.isValid()).isFalse();
    }
}
