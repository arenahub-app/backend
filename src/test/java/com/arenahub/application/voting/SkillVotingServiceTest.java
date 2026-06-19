package com.arenahub.application.voting;

import com.arenahub.application.exception.*;
import com.arenahub.application.voting.port.in.*;
import com.arenahub.application.voting.port.out.GroupMemberSkillPort;
import com.arenahub.application.voting.port.out.GroupMemberSkillPort.GroupMemberView;
import com.arenahub.application.voting.port.out.SkillVotingRepository;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.voting.SkillVoting;
import com.arenahub.domain.voting.Vote;
import com.arenahub.domain.voting.VotingBan;
import com.arenahub.domain.voting.service.SkillCalculationService;
import com.arenahub.domain.voting.vo.Stars;
import com.arenahub.domain.voting.vo.VotingStatus;
import com.arenahub.presentation.voting.dto.VoteResponse;
import com.arenahub.presentation.voting.dto.VotingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillVotingServiceTest {

    @Mock
    SkillVotingRepository votingRepo;

    @Mock
    GroupMemberSkillPort memberPort;

    @Mock
    SkillCalculationService calculationService;

    SkillVotingService service;

    UUID groupId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID adminMemberId = UUID.randomUUID();
    UUID playerMemberId = UUID.randomUUID();
    UUID targetMemberId = UUID.randomUUID();
    UUID votingId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SkillVotingService(votingRepo, memberPort, calculationService);
    }

    private GroupMemberView adminView() {
        return new GroupMemberView(adminMemberId, userId, "Admin User", null,
                GroupRole.ADMIN, BigDecimal.valueOf(4.0));
    }

    private GroupMemberView playerView(UUID memberId, UUID uid) {
        return new GroupMemberView(memberId, uid, "Player", null,
                GroupRole.PLAYER, BigDecimal.valueOf(3.0));
    }

    private GroupMemberView refereeView() {
        UUID refMemberId = UUID.randomUUID();
        UUID refUserId = UUID.randomUUID();
        return new GroupMemberView(refMemberId, refUserId, "Referee", null,
                GroupRole.REFEREE, BigDecimal.valueOf(3.0));
    }

    private SkillVoting openVoting() {
        return SkillVoting.reconstitute(votingId, groupId, VotingStatus.OPEN,
                userId, Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(2, ChronoUnit.HOURS), null);
    }

    @Test
    void openVoting_throwsWhenAlreadyActive() {
        when(memberPort.findMemberByGroupAndUser(groupId, userId)).thenReturn(Optional.of(adminView()));
        when(votingRepo.findActiveByGroupId(groupId)).thenReturn(Optional.of(openVoting()));

        assertThatThrownBy(() -> service.execute(
                new OpenVotingUseCase.Command(groupId, userId, Instant.now().plus(2, ChronoUnit.HOURS))))
                .isInstanceOf(VotingAlreadyOpenException.class);
    }

    @Test
    void openVoting_throwsWhenPlayerTriesToOpen() {
        when(memberPort.findMemberByGroupAndUser(groupId, userId))
                .thenReturn(Optional.of(playerView(playerMemberId, userId)));

        assertThatThrownBy(() -> service.execute(
                new OpenVotingUseCase.Command(groupId, userId, Instant.now().plus(2, ChronoUnit.HOURS))))
                .isInstanceOf(InsufficientRoleException.class);
    }

    @Test
    void openVoting_throwsWhenNotMember() {
        when(memberPort.findMemberByGroupAndUser(groupId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new OpenVotingUseCase.Command(groupId, userId, Instant.now().plus(2, ChronoUnit.HOURS))))
                .isInstanceOf(NotAMemberException.class);
    }

    @Test
    void castVote_throwsWhenVotingClosed() {
        SkillVoting closed = SkillVoting.reconstitute(votingId, groupId, VotingStatus.CLOSED,
                userId, Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.HOURS), Instant.now().minus(30, ChronoUnit.MINUTES));

        when(memberPort.findMemberByGroupAndUser(groupId, userId))
                .thenReturn(Optional.of(playerView(playerMemberId, userId)));
        when(votingRepo.findByIdAndGroupId(votingId, groupId)).thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> service.execute(
                new CastVoteUseCase.Command(groupId, votingId, targetMemberId, userId, 4)))
                .isInstanceOf(VotingClosedException.class);
    }

    @Test
    void castVote_throwsWhenReferee() {
        when(memberPort.findMemberByGroupAndUser(groupId, userId))
                .thenReturn(Optional.of(refereeView()));

        assertThatThrownBy(() -> service.execute(
                new CastVoteUseCase.Command(groupId, votingId, targetMemberId, userId, 4)))
                .isInstanceOf(InsufficientRoleException.class);
    }

    @Test
    void castVote_throwsWhenSelfVote() {
        when(memberPort.findMemberByGroupAndUser(groupId, userId))
                .thenReturn(Optional.of(playerView(playerMemberId, userId)));
        when(votingRepo.findByIdAndGroupId(votingId, groupId)).thenReturn(Optional.of(openVoting()));
        when(votingRepo.findBan(votingId, playerMemberId)).thenReturn(Optional.empty());

        GroupMemberView selfTarget = playerView(playerMemberId, userId);
        when(memberPort.findMemberById(playerMemberId)).thenReturn(Optional.of(selfTarget));

        assertThatThrownBy(() -> service.execute(
                new CastVoteUseCase.Command(groupId, votingId, playerMemberId, userId, 4)))
                .isInstanceOf(SelfVoteNotAllowedException.class);
    }

    @Test
    void castVote_throwsWhenVoterIsBanned() {
        GroupMemberView voter = playerView(playerMemberId, userId);
        when(memberPort.findMemberByGroupAndUser(groupId, userId)).thenReturn(Optional.of(voter));
        when(votingRepo.findByIdAndGroupId(votingId, groupId)).thenReturn(Optional.of(openVoting()));

        VotingBan ban = VotingBan.ban(votingId, groupId, playerMemberId, "motivo", UUID.randomUUID());
        when(votingRepo.findBan(votingId, playerMemberId)).thenReturn(Optional.of(ban));

        assertThatThrownBy(() -> service.execute(
                new CastVoteUseCase.Command(groupId, votingId, targetMemberId, userId, 4)))
                .isInstanceOf(VoterIsBannedException.class);
    }

    @Test
    void castVote_createsNewVoteWhenNoneExists() {
        GroupMemberView voter = playerView(playerMemberId, userId);
        GroupMemberView target = playerView(targetMemberId, UUID.randomUUID());

        when(memberPort.findMemberByGroupAndUser(groupId, userId)).thenReturn(Optional.of(voter));
        when(votingRepo.findByIdAndGroupId(votingId, groupId)).thenReturn(Optional.of(openVoting()));
        when(votingRepo.findBan(votingId, playerMemberId)).thenReturn(Optional.empty());
        when(memberPort.findMemberById(targetMemberId)).thenReturn(Optional.of(target));
        when(votingRepo.findVote(votingId, playerMemberId, targetMemberId)).thenReturn(Optional.empty());

        Vote savedVote = Vote.cast(votingId, groupId, playerMemberId, targetMemberId, new Stars(4));
        when(votingRepo.saveVote(any())).thenReturn(savedVote);

        VoteResponse response = service.execute(
                new CastVoteUseCase.Command(groupId, votingId, targetMemberId, userId, 4));

        assertThat(response.stars()).isEqualTo(4);
        verify(votingRepo).saveVote(any());
    }

    @Test
    void castVote_updatesExistingVote() {
        GroupMemberView voter = playerView(playerMemberId, userId);
        GroupMemberView target = playerView(targetMemberId, UUID.randomUUID());
        Vote existing = Vote.cast(votingId, groupId, playerMemberId, targetMemberId, new Stars(2));

        when(memberPort.findMemberByGroupAndUser(groupId, userId)).thenReturn(Optional.of(voter));
        when(votingRepo.findByIdAndGroupId(votingId, groupId)).thenReturn(Optional.of(openVoting()));
        when(votingRepo.findBan(votingId, playerMemberId)).thenReturn(Optional.empty());
        when(memberPort.findMemberById(targetMemberId)).thenReturn(Optional.of(target));
        when(votingRepo.findVote(votingId, playerMemberId, targetMemberId)).thenReturn(Optional.of(existing));
        when(votingRepo.saveVote(any())).thenReturn(existing);

        service.execute(new CastVoteUseCase.Command(groupId, votingId, targetMemberId, userId, 5));

        verify(votingRepo).saveVote(argThat(v -> v.getStars().value() == 5));
    }

    @Test
    void closeVoting_throwsWhenAlreadyClosed() {
        SkillVoting closed = SkillVoting.reconstitute(votingId, groupId, VotingStatus.CLOSED,
                userId, Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.HOURS), Instant.now().minus(30, ChronoUnit.MINUTES));

        when(memberPort.findMemberByGroupAndUser(groupId, userId)).thenReturn(Optional.of(adminView()));
        when(votingRepo.findByIdAndGroupId(votingId, groupId)).thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> service.execute(
                new CloseVotingUseCase.Command(groupId, votingId, userId)))
                .isInstanceOf(VotingAlreadyClosedException.class);
    }

    @Test
    void banFromVoting_throwsWhenAlreadyBanned() {
        UUID bannedMemberId = UUID.randomUUID();
        VotingBan existingBan = VotingBan.ban(votingId, groupId, bannedMemberId, "motivo", userId);

        when(memberPort.findMemberByGroupAndUser(groupId, userId)).thenReturn(Optional.of(adminView()));
        when(votingRepo.findByIdAndGroupId(votingId, groupId)).thenReturn(Optional.of(openVoting()));
        when(memberPort.findMemberById(bannedMemberId))
                .thenReturn(Optional.of(playerView(bannedMemberId, UUID.randomUUID())));
        when(votingRepo.findBan(votingId, bannedMemberId)).thenReturn(Optional.of(existingBan));

        assertThatThrownBy(() -> service.execute(
                new BanFromVotingUseCase.Command(groupId, votingId, bannedMemberId, "motivo", userId)))
                .isInstanceOf(MemberAlreadyBannedFromVotingException.class);
    }

    @Test
    void unbanFromVoting_throwsWhenBanNotFound() {
        UUID unbannedMemberId = UUID.randomUUID();

        when(memberPort.findMemberByGroupAndUser(groupId, userId)).thenReturn(Optional.of(adminView()));
        when(votingRepo.findByIdAndGroupId(votingId, groupId)).thenReturn(Optional.of(openVoting()));
        when(votingRepo.findBan(votingId, unbannedMemberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new UnbanFromVotingUseCase.Command(groupId, votingId, unbannedMemberId, userId)))
                .isInstanceOf(VotingBanNotFoundException.class);
    }

    @Test
    void getResults_throwsWhenPlayerTriesPartialResults() {
        when(memberPort.findMemberByGroupAndUser(groupId, userId))
                .thenReturn(Optional.of(playerView(playerMemberId, userId)));
        when(votingRepo.findByIdAndGroupId(votingId, groupId)).thenReturn(Optional.of(openVoting()));

        assertThatThrownBy(() -> service.execute(
                new GetVotingResultsUseCase.Command(groupId, votingId, userId)))
                .isInstanceOf(ResultsNotAvailableException.class);
    }
}
