package com.arenahub.domain.voting;

import com.arenahub.domain.voting.vo.Stars;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class VoteTest {

    private static final UUID VOTING_ID = UUID.randomUUID();
    private static final UUID GROUP_ID = UUID.randomUUID();
    private static final UUID VOTER_ID = UUID.randomUUID();
    private static final UUID TARGET_ID = UUID.randomUUID();

    @Test
    void cast_setsAllFields() {
        Stars stars = new Stars(4);
        Vote vote = Vote.cast(VOTING_ID, GROUP_ID, VOTER_ID, TARGET_ID, stars);

        assertThat(vote.getVotingId()).isEqualTo(VOTING_ID);
        assertThat(vote.getVoterId()).isEqualTo(VOTER_ID);
        assertThat(vote.getTargetMemberId()).isEqualTo(TARGET_ID);
        assertThat(vote.getStars().value()).isEqualTo(4);
        assertThat(vote.getVotedAt()).isNotNull();
    }

    @Test
    void cast_throwsOnSelfVote() {
        assertThatThrownBy(() -> Vote.cast(VOTING_ID, GROUP_ID, VOTER_ID, VOTER_ID, new Stars(3)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_changesStars() {
        Vote vote = Vote.cast(VOTING_ID, GROUP_ID, VOTER_ID, TARGET_ID, new Stars(2));
        vote.update(new Stars(5));

        assertThat(vote.getStars().value()).isEqualTo(5);
    }
}
