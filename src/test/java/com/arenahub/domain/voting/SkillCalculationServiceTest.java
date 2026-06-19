package com.arenahub.domain.voting;

import com.arenahub.application.voting.port.out.GroupMemberSkillPort;
import com.arenahub.application.voting.port.out.SkillVotingRepository;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.Skill;
import com.arenahub.domain.group.vo.SkillSource;
import com.arenahub.domain.voting.service.SkillCalculationService;
import com.arenahub.domain.voting.vo.Stars;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillCalculationServiceTest {

    @Mock
    SkillVotingRepository repo;

    @Mock
    GroupMemberSkillPort memberPort;

    SkillCalculationService service;

    UUID votingId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    UUID openedBy = UUID.randomUUID();

    UUID memberA = UUID.randomUUID();
    UUID memberB = UUID.randomUUID();
    UUID memberC = UUID.randomUUID();
    UUID voterBanned = UUID.randomUUID();

    SkillVoting voting;

    @BeforeEach
    void setUp() {
        service = new SkillCalculationService();
        voting = SkillVoting.reconstitute(votingId, groupId, com.arenahub.domain.voting.vo.VotingStatus.CLOSED,
                openedBy, Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().minus(1, ChronoUnit.HOURS), Instant.now());
    }

    @Test
    void calculateAndUpdate_computesAverageAndUpdates() {
        Vote v1 = Vote.reconstitute(UUID.randomUUID(), votingId, groupId,
                memberB, memberA, new Stars(4), Instant.now(), Instant.now());
        Vote v2 = Vote.reconstitute(UUID.randomUUID(), votingId, groupId,
                memberC, memberA, new Stars(6), Instant.now(), Instant.now());

        when(repo.findVotesByVotingId(votingId)).thenReturn(List.of(v1, v2));
        when(repo.findBansByVotingId(votingId)).thenReturn(List.of());

        Map<UUID, BigDecimal> result = service.calculateAndUpdate(voting, repo, memberPort);

        assertThat(result).containsKey(memberA);
        assertThat(result.get(memberA)).isEqualByComparingTo(new BigDecimal("5.0"));

        ArgumentCaptor<Skill> skillCaptor = ArgumentCaptor.forClass(Skill.class);
        verify(memberPort).updateSkill(eq(memberA), skillCaptor.capture(), eq(SkillSource.VOTING));
        assertThat(skillCaptor.getValue().value()).isEqualByComparingTo(new BigDecimal("5.0"));
    }

    @Test
    void calculateAndUpdate_excludesBannedVoterVotes() {
        Vote v1 = Vote.reconstitute(UUID.randomUUID(), votingId, groupId,
                memberB, memberA, new Stars(6), Instant.now(), Instant.now());
        Vote v2 = Vote.reconstitute(UUID.randomUUID(), votingId, groupId,
                voterBanned, memberA, new Stars(1), Instant.now(), Instant.now());

        VotingBan ban = VotingBan.reconstitute(UUID.randomUUID(), votingId, groupId,
                voterBanned, "motivo", openedBy, Instant.now());

        when(repo.findVotesByVotingId(votingId)).thenReturn(List.of(v1, v2));
        when(repo.findBansByVotingId(votingId)).thenReturn(List.of(ban));

        Map<UUID, BigDecimal> result = service.calculateAndUpdate(voting, repo, memberPort);

        assertThat(result.get(memberA)).isEqualByComparingTo(new BigDecimal("6.0"));
    }

    @Test
    void calculateAndUpdate_doesNotUpdateMembersWithNoVotes() {
        when(repo.findVotesByVotingId(votingId)).thenReturn(List.of());
        when(repo.findBansByVotingId(votingId)).thenReturn(List.of());

        Map<UUID, BigDecimal> result = service.calculateAndUpdate(voting, repo, memberPort);

        assertThat(result).isEmpty();
        verifyNoInteractions(memberPort);
    }

    @Test
    void calculateAndUpdate_roundsToOneDecimal() {
        Vote v1 = Vote.reconstitute(UUID.randomUUID(), votingId, groupId,
                memberB, memberA, new Stars(4), Instant.now(), Instant.now());
        Vote v2 = Vote.reconstitute(UUID.randomUUID(), votingId, groupId,
                memberC, memberA, new Stars(5), Instant.now(), Instant.now());
        Vote v3 = Vote.reconstitute(UUID.randomUUID(), votingId, groupId,
                openedBy, memberA, new Stars(3), Instant.now(), Instant.now());

        when(repo.findVotesByVotingId(votingId)).thenReturn(List.of(v1, v2, v3));
        when(repo.findBansByVotingId(votingId)).thenReturn(List.of());

        Map<UUID, BigDecimal> result = service.calculateAndUpdate(voting, repo, memberPort);

        assertThat(result.get(memberA)).isEqualByComparingTo(new BigDecimal("4.0"));
    }
}
