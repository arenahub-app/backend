package com.arenahub.domain.match;

import com.arenahub.domain.match.vo.JustificationStatus;
import com.arenahub.domain.match.vo.PresenceStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PresenceEntryTest {

    private static final UUID MATCH_ID = UUID.randomUUID();
    private static final UUID GROUP_ID = UUID.randomUUID();
    private static final UUID MEMBER_ID = UUID.randomUUID();

    @Test
    void confirm_setsStatusAndConfirmedAt() {
        PresenceEntry entry = PresenceEntry.confirm(MATCH_ID, GROUP_ID, MEMBER_ID);

        assertThat(entry.getStatus()).isEqualTo(PresenceStatus.CONFIRMED);
        assertThat(entry.getConfirmedAt()).isNotNull();
    }

    @Test
    void decline_setsStatusDeclined() {
        PresenceEntry entry = PresenceEntry.decline(MATCH_ID, GROUP_ID, MEMBER_ID);

        assertThat(entry.getStatus()).isEqualTo(PresenceStatus.DECLINED);
        assertThat(entry.getConfirmedAt()).isNull();
    }

    @Test
    void pendingBan_setsStatusAndJustification() {
        PresenceEntry entry = PresenceEntry.pendingBan(MATCH_ID, GROUP_ID, MEMBER_ID, "Ausências repetidas");

        assertThat(entry.getStatus()).isEqualTo(PresenceStatus.BANNED_PENDING);
        assertThat(entry.getJustification()).isEqualTo("Ausências repetidas");
        assertThat(entry.getJustificationStatus()).isEqualTo(JustificationStatus.PENDING);
    }
}
