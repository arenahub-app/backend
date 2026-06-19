package com.arenahub.domain.voting;

import com.arenahub.domain.voting.vo.VotingStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SkillVotingTest {

    private static final UUID GROUP_ID = UUID.randomUUID();
    private static final UUID OPENED_BY = UUID.randomUUID();

    private Instant futureDeadline() {
        return Instant.now().plus(2, ChronoUnit.HOURS);
    }

    @Test
    void open_setsStatusAndFields() {
        SkillVoting voting = SkillVoting.open(GROUP_ID, futureDeadline(), OPENED_BY);

        assertThat(voting.getStatus()).isEqualTo(VotingStatus.OPEN);
        assertThat(voting.getGroupId()).isEqualTo(GROUP_ID);
        assertThat(voting.getOpenedBy()).isEqualTo(OPENED_BY);
        assertThat(voting.getClosedAt()).isNull();
        assertThat(voting.isOpen()).isTrue();
    }

    @Test
    void open_throwsWhenDeadlineTooSoon() {
        Instant tooSoon = Instant.now().plus(10, ChronoUnit.MINUTES);

        assertThatThrownBy(() -> SkillVoting.open(GROUP_ID, tooSoon, OPENED_BY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void close_setsStatusAndClosedAt() {
        SkillVoting voting = SkillVoting.open(GROUP_ID, futureDeadline(), OPENED_BY);
        Instant now = Instant.now();
        voting.close(now);

        assertThat(voting.getStatus()).isEqualTo(VotingStatus.CLOSED);
        assertThat(voting.getClosedAt()).isEqualTo(now);
        assertThat(voting.isOpen()).isFalse();
    }

    @Test
    void close_throwsWhenAlreadyClosed() {
        SkillVoting voting = SkillVoting.open(GROUP_ID, futureDeadline(), OPENED_BY);
        voting.close(Instant.now());

        assertThatThrownBy(() -> voting.close(Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }
}
