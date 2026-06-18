package com.arenahub.domain.payment;

import com.arenahub.domain.payment.vo.ChargeStatus;
import com.arenahub.domain.payment.vo.ChargeType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ChargeTest {

    private final UUID groupId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();
    private final UUID matchId = UUID.randomUUID();

    @Test
    void createDaily_setsCorrectFields() {
        Charge charge = Charge.createDaily(groupId, memberId, new BigDecimal("25.00"), matchId);

        assertThat(charge.getId()).isNotNull();
        assertThat(charge.getGroupId()).isEqualTo(groupId);
        assertThat(charge.getMemberId()).isEqualTo(memberId);
        assertThat(charge.getType()).isEqualTo(ChargeType.DAILY);
        assertThat(charge.getAmount()).isEqualByComparingTo("25.00");
        assertThat(charge.getReferenceMatchId()).isEqualTo(matchId);
        assertThat(charge.getStatus()).isEqualTo(ChargeStatus.PENDING);
        assertThat(charge.getCreatedAt()).isNotNull();
    }

    @Test
    void approve_changesStatusToApproved() {
        Charge charge = Charge.createDaily(groupId, memberId, new BigDecimal("25.00"), matchId);

        charge.approve();

        assertThat(charge.getStatus()).isEqualTo(ChargeStatus.APPROVED);
        assertThat(charge.isPending()).isFalse();
    }

    @Test
    void approve_throwsWhenAlreadyApproved() {
        Charge charge = Charge.createDaily(groupId, memberId, new BigDecimal("25.00"), matchId);
        charge.approve();

        assertThatThrownBy(charge::approve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("charge-already-approved");
    }

    @Test
    void isPending_returnsTrueWhenPending() {
        Charge charge = Charge.createDaily(groupId, memberId, new BigDecimal("10.00"), matchId);
        assertThat(charge.isPending()).isTrue();
    }

    @Test
    void reconstitute_restoresAllFields() {
        UUID id = UUID.randomUUID();
        var now = java.time.Instant.now();
        Charge charge = Charge.reconstitute(id, groupId, memberId, null, ChargeType.DAILY,
                new BigDecimal("30.00"), matchId, null, ChargeStatus.APPROVED, now, now);

        assertThat(charge.getId()).isEqualTo(id);
        assertThat(charge.getStatus()).isEqualTo(ChargeStatus.APPROVED);
        assertThat(charge.isPending()).isFalse();
    }

    @Test
    void createDailyForGuest_setsCorrectFields() {
        UUID guestId = UUID.randomUUID();
        Charge charge = Charge.createDailyForGuest(groupId, guestId, new BigDecimal("25.00"), matchId);

        assertThat(charge.getId()).isNotNull();
        assertThat(charge.getGroupId()).isEqualTo(groupId);
        assertThat(charge.getMemberId()).isNull();
        assertThat(charge.getGuestId()).isEqualTo(guestId);
        assertThat(charge.getType()).isEqualTo(ChargeType.DAILY);
        assertThat(charge.getAmount()).isEqualByComparingTo("25.00");
        assertThat(charge.getReferenceMatchId()).isEqualTo(matchId);
        assertThat(charge.getStatus()).isEqualTo(ChargeStatus.PENDING);
    }

    @Test
    void isGuestCharge_returnsTrueForGuestCharge() {
        UUID guestId = UUID.randomUUID();
        Charge charge = Charge.createDailyForGuest(groupId, guestId, new BigDecimal("25.00"), matchId);
        assertThat(charge.isGuestCharge()).isTrue();
        assertThat(charge.getGuestId()).isEqualTo(guestId);
    }

    @Test
    void isGuestCharge_returnsFalseForMemberCharge() {
        Charge charge = Charge.createDaily(groupId, memberId, new BigDecimal("25.00"), matchId);
        assertThat(charge.isGuestCharge()).isFalse();
    }
}
