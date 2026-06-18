package com.arenahub.domain.match;

import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.domain.match.vo.GuestStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class MatchGuestTest {

    private final UUID matchId = UUID.randomUUID();
    private final UUID groupId = UUID.randomUUID();
    private final UUID addedBy = UUID.randomUUID();

    @Test
    void add_withFee_createsPaymentPendingGuest() {
        MatchGuest guest = MatchGuest.add(matchId, groupId, "Carlos", new BigDecimal("3.5"),
                PlayerPosition.MIDFIELDER, addedBy, true);

        assertThat(guest.getId()).isNotNull();
        assertThat(guest.getStatus()).isEqualTo(GuestStatus.PAYMENT_PENDING);
        assertThat(guest.getConfirmedAt()).isNull();
        assertThat(guest.getName()).isEqualTo("Carlos");
        assertThat(guest.getSkill()).isEqualByComparingTo("3.5");
        assertThat(guest.getCreatedAt()).isNotNull();
    }

    @Test
    void add_withoutFee_createsConfirmedGuest() {
        MatchGuest guest = MatchGuest.add(matchId, groupId, "Ana", new BigDecimal("4.0"),
                PlayerPosition.FORWARD, addedBy, false);

        assertThat(guest.getStatus()).isEqualTo(GuestStatus.CONFIRMED);
        assertThat(guest.getConfirmedAt()).isNotNull();
    }

    @Test
    void add_invalidSkill_throwsException() {
        assertThatThrownBy(() -> MatchGuest.add(matchId, groupId, "Joao",
                new BigDecimal("0.5"), PlayerPosition.MIDFIELDER, addedBy, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid-skill");

        assertThatThrownBy(() -> MatchGuest.add(matchId, groupId, "Joao",
                new BigDecimal("5.5"), PlayerPosition.MIDFIELDER, addedBy, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid-skill");
    }

    @Test
    void confirmAfterPayment_updatesStatus() {
        MatchGuest guest = MatchGuest.add(matchId, groupId, "Pedro", new BigDecimal("3.0"),
                PlayerPosition.DEFENDER, addedBy, true);

        assertThat(guest.getStatus()).isEqualTo(GuestStatus.PAYMENT_PENDING);
        assertThat(guest.getConfirmedAt()).isNull();

        guest.confirmAfterPayment();

        assertThat(guest.getStatus()).isEqualTo(GuestStatus.CONFIRMED);
        assertThat(guest.getConfirmedAt()).isNotNull();
    }
}
