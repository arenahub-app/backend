package com.arenahub.domain.match;

import com.arenahub.domain.match.vo.Location;
import com.arenahub.domain.match.vo.MatchStatus;
import com.arenahub.domain.match.vo.PresenceListStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class MatchTest {

    private static final UUID GROUP_ID = UUID.randomUUID();
    private static final UUID CREATOR = UUID.randomUUID();
    private static final Location LOCATION = new Location("Quadra Central", "Av. 1");

    private Instant futureDate() {
        return Instant.now().plus(Duration.ofHours(2));
    }

    @Test
    void create_setsScheduledAndOpenList() {
        Match match = Match.create(GROUP_ID, futureDate(), LOCATION, 10, CREATOR);

        assertThat(match.getStatus()).isEqualTo(MatchStatus.SCHEDULED);
        assertThat(match.getPresenceListStatus()).isEqualTo(PresenceListStatus.OPEN);
        assertThat(match.getListClosesAt()).isEqualTo(match.getScheduledAt().minus(Duration.ofHours(1)));
        assertThat(match.isListOpen()).isTrue();
    }

    @Test
    void create_throwsWhenScheduledAtTooSoon() {
        Instant soon = Instant.now().plus(Duration.ofMinutes(10));

        assertThatThrownBy(() -> Match.create(GROUP_ID, soon, LOCATION, 10, CREATOR))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancel_setsStatusCancelledAndListClosed() {
        Match match = Match.create(GROUP_ID, futureDate(), LOCATION, 10, CREATOR);
        match.cancel();

        assertThat(match.getStatus()).isEqualTo(MatchStatus.CANCELLED);
        assertThat(match.getPresenceListStatus()).isEqualTo(PresenceListStatus.CLOSED);
    }

    @Test
    void cancel_throwsWhenAlreadyCancelled() {
        Match match = Match.create(GROUP_ID, futureDate(), LOCATION, 10, CREATOR);
        match.cancel();

        assertThatThrownBy(match::cancel).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("match-already-cancelled");
    }

    @Test
    void closePresenceList_changesStatusToClosed() {
        Match match = Match.create(GROUP_ID, futureDate(), LOCATION, 10, CREATOR);
        match.closePresenceList();

        assertThat(match.getPresenceListStatus()).isEqualTo(PresenceListStatus.CLOSED);
        assertThat(match.isListOpen()).isFalse();
    }

    @Test
    void closePresenceList_throwsWhenAlreadyClosed() {
        Match match = Match.create(GROUP_ID, futureDate(), LOCATION, 10, CREATOR);
        match.closePresenceList();

        assertThatThrownBy(match::closePresenceList).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("list-already-closed");
    }

    @Test
    void update_recalculatesListClosesAt() {
        Match match = Match.create(GROUP_ID, futureDate(), LOCATION, 10, CREATOR);
        Instant newDate = futureDate().plus(Duration.ofDays(1));

        match.update(newDate, null, null);

        assertThat(match.getScheduledAt()).isEqualTo(newDate);
        assertThat(match.getListClosesAt()).isEqualTo(newDate.minus(Duration.ofHours(1)));
    }

    @Test
    void update_throwsWhenListClosed() {
        Match match = Match.create(GROUP_ID, futureDate(), LOCATION, 10, CREATOR);
        match.closePresenceList();

        assertThatThrownBy(() -> match.update(futureDate(), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("match-not-editable");
    }
}
