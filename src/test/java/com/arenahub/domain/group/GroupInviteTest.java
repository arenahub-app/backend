package com.arenahub.domain.group;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class GroupInviteTest {

    @Test
    void create_setsActiveWithTokenAndExpiry() {
        UUID groupId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        GroupInvite invite = GroupInvite.create(groupId, createdBy);

        assertThat(invite.getId()).isNotNull();
        assertThat(invite.getToken()).isNotBlank();
        assertThat(invite.isActive()).isTrue();
        assertThat(invite.getUsageCount()).isZero();
        assertThat(invite.getMaxUsages()).isEqualTo(50);
        assertThat(invite.getExpiresAt()).isAfter(Instant.now().plus(6, ChronoUnit.DAYS));
        assertThat(invite.isValid()).isTrue();
    }

    @Test
    void incrementUsage_increasesCount() {
        GroupInvite invite = GroupInvite.create(UUID.randomUUID(), UUID.randomUUID());

        invite.incrementUsage();

        assertThat(invite.getUsageCount()).isEqualTo(1);
        assertThat(invite.isValid()).isTrue();
    }

    @Test
    void deactivate_invalidatesInvite() {
        GroupInvite invite = GroupInvite.create(UUID.randomUUID(), UUID.randomUUID());

        invite.deactivate();

        assertThat(invite.isActive()).isFalse();
        assertThat(invite.isValid()).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenExpired() {
        GroupInvite invite = GroupInvite.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "token", 0, 50, true,
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().minus(8, ChronoUnit.DAYS));

        assertThat(invite.isValid()).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenMaxUsagesReached() {
        GroupInvite invite = GroupInvite.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "token", 50, 50, true,
                Instant.now().plus(7, ChronoUnit.DAYS),
                Instant.now());

        assertThat(invite.isValid()).isFalse();
    }
}
