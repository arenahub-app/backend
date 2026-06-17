package com.arenahub.application.match;

import com.arenahub.application.exception.*;
import com.arenahub.application.match.port.in.*;
import com.arenahub.application.match.port.out.ChargePort;
import com.arenahub.application.match.port.out.GroupMemberPort;
import com.arenahub.application.match.port.out.GroupMemberPort.GroupMemberView;
import com.arenahub.application.match.port.out.MatchRepository;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.SkillSource;
import com.arenahub.domain.match.Match;
import com.arenahub.domain.match.PresenceEntry;
import com.arenahub.domain.match.WaitingEntry;
import com.arenahub.domain.match.vo.Location;
import com.arenahub.domain.match.vo.PresenceListStatus;
import com.arenahub.domain.match.vo.PresenceStatus;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.infrastructure.persistence.group.GroupJpaEntity;
import com.arenahub.infrastructure.persistence.group.GroupJpaRepository;
import com.arenahub.presentation.match.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    MatchRepository matchRepository;

    @Mock
    GroupMemberPort groupMemberPort;

    @Mock
    GroupJpaRepository groupRepo;

    @Mock
    ChargePort chargePort;

    private MatchService matchService;

    private final UUID groupId = UUID.randomUUID();
    private final UUID actorId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        matchService = new MatchService(matchRepository, groupMemberPort, groupRepo, chargePort);
    }

    private GroupMemberView adminView() {
        return new GroupMemberView(memberId, actorId, groupId, "Admin User",
                GroupRole.ADMIN, BigDecimal.valueOf(5.0), SkillSource.MANUAL, null,
                false, Instant.now(), false, null);
    }

    private GroupMemberView playerView(UUID userId) {
        UUID mid = UUID.randomUUID();
        return new GroupMemberView(mid, userId, groupId, "Player",
                GroupRole.PLAYER, BigDecimal.valueOf(3.0), SkillSource.DEFAULT, null,
                false, Instant.now(), false, null);
    }

    private GroupMemberView refereeView() {
        return new GroupMemberView(UUID.randomUUID(), actorId, groupId, "Ref",
                GroupRole.REFEREE, BigDecimal.ZERO, SkillSource.DEFAULT, null,
                false, Instant.now(), false, null);
    }

    private GroupMemberView bannedView() {
        return new GroupMemberView(memberId, actorId, groupId, "Banned",
                GroupRole.PLAYER, BigDecimal.valueOf(3.0), SkillSource.DEFAULT, null,
                false, Instant.now(), true, "Ausências repetidas");
    }

    private Match scheduledMatch(int maxPlayers) {
        return Match.create(groupId, Instant.now().plus(Duration.ofHours(2)),
                new Location("Quadra", null), maxPlayers, memberId);
    }

    private GroupJpaEntity activeGroup() {
        GroupJpaEntity g = new GroupJpaEntity();
        g.setStatus(GroupStatus.ACTIVE);
        return g;
    }

    // ── Create Match ──────────────────────────────────────────────────────────

    @Test
    void createMatch_returnsMatchResponse() {
        when(groupRepo.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(activeGroup()));
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        when(matchRepository.countOccupiedByMatchId(any())).thenReturn(0L);
        when(matchRepository.countWaitingByMatchId(any())).thenReturn(0L);

        MatchResponse response = matchService.execute(new CreateMatchUseCase.Command(
                groupId, actorId, Instant.now().plus(Duration.ofHours(2)),
                "Quadra", "Av. 1", 10));

        assertThat(response.maxPlayers()).isEqualTo(10);
        assertThat(response.presenceListStatus()).isEqualTo(PresenceListStatus.OPEN);
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    void createMatch_throwsInsufficientRole_whenPlayer() {
        when(groupRepo.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(activeGroup()));
        when(groupMemberPort.findMember(groupId, actorId))
                .thenReturn(Optional.of(playerView(actorId)));

        assertThatThrownBy(() -> matchService.execute(new CreateMatchUseCase.Command(
                groupId, actorId, Instant.now().plus(Duration.ofHours(2)),
                "Quadra", null, 10)))
                .isInstanceOf(InsufficientRoleException.class);
    }

    @Test
    void createMatch_throwsMatchInPast_whenDateTooSoon() {
        when(groupRepo.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(activeGroup()));
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));

        Instant tooSoon = Instant.now().plus(Duration.ofMinutes(5));

        assertThatThrownBy(() -> matchService.execute(new CreateMatchUseCase.Command(
                groupId, actorId, tooSoon, "Quadra", null, 10)))
                .isInstanceOf(MatchInPastException.class);
    }

    // ── Confirm Presence ──────────────────────────────────────────────────────

    @Test
    void confirmPresence_createsEntryWhenVacancyAvailable() {
        Match match = scheduledMatch(5);
        UUID matchId = match.getId();

        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        when(matchRepository.findByIdAndGroupId(matchId, groupId)).thenReturn(Optional.of(match));
        when(matchRepository.findPresenceEntry(matchId, memberId)).thenReturn(Optional.empty());
        when(matchRepository.countOccupiedByMatchId(matchId)).thenReturn(2L);
        when(matchRepository.savePresenceEntry(any())).thenAnswer(inv -> inv.getArgument(0));
        when(groupMemberPort.findMemberById(memberId)).thenReturn(Optional.of(adminView()));

        PresenceActionResponse result = matchService.execute(
                new ConfirmPresenceUseCase.Command(groupId, matchId, actorId));

        assertThat(result.type()).isEqualTo("PRESENCE");
        verify(matchRepository).savePresenceEntry(argThat(e -> e.getStatus() == PresenceStatus.CONFIRMED));
    }

    @Test
    void confirmPresence_entersQueueWhenMatchFull() {
        Match match = scheduledMatch(2);
        UUID matchId = match.getId();

        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        when(matchRepository.findByIdAndGroupId(matchId, groupId)).thenReturn(Optional.of(match));
        when(matchRepository.findPresenceEntry(matchId, memberId)).thenReturn(Optional.empty());
        when(matchRepository.countOccupiedByMatchId(matchId)).thenReturn(2L);
        when(matchRepository.countWaitingByMatchId(matchId)).thenReturn(0L);
        when(matchRepository.saveWaitingEntry(any())).thenAnswer(inv -> inv.getArgument(0));
        when(groupMemberPort.findMemberById(memberId)).thenReturn(Optional.of(adminView()));

        PresenceActionResponse result = matchService.execute(
                new ConfirmPresenceUseCase.Command(groupId, matchId, actorId));

        assertThat(result.type()).isEqualTo("WAITING");
        verify(matchRepository).saveWaitingEntry(any(WaitingEntry.class));
    }

    @Test
    void confirmPresence_throwsListClosed_whenListNotOpen() {
        Match match = scheduledMatch(10);
        match.closePresenceList();
        UUID matchId = match.getId();

        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        when(matchRepository.findByIdAndGroupId(matchId, groupId)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.execute(
                new ConfirmPresenceUseCase.Command(groupId, matchId, actorId)))
                .isInstanceOf(ListClosedException.class);
    }

    @Test
    void confirmPresence_throwsRefereeCannotConfirm() {
        Match match = scheduledMatch(10);
        UUID matchId = match.getId();

        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(refereeView()));

        assertThatThrownBy(() -> matchService.execute(
                new ConfirmPresenceUseCase.Command(groupId, matchId, actorId)))
                .isInstanceOf(RefereeCannotConfirmException.class);
    }

    @Test
    void confirmPresence_createsBannedPendingEntry_whenMemberBanned() {
        Match match = scheduledMatch(10);
        UUID matchId = match.getId();
        GroupMemberView banned = bannedView();

        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(banned));
        when(matchRepository.findByIdAndGroupId(matchId, groupId)).thenReturn(Optional.of(match));
        when(matchRepository.findPresenceEntry(matchId, banned.id())).thenReturn(Optional.empty());
        when(matchRepository.savePresenceEntry(any())).thenAnswer(inv -> inv.getArgument(0));
        when(groupMemberPort.findMemberById(banned.id())).thenReturn(Optional.of(banned));

        PresenceActionResponse result = matchService.execute(
                new ConfirmPresenceUseCase.Command(groupId, matchId, actorId));

        assertThat(result.type()).isEqualTo("PRESENCE");
        verify(matchRepository).savePresenceEntry(
                argThat(e -> e.getStatus() == PresenceStatus.BANNED_PENDING));
    }

    // ── Cancel Presence ───────────────────────────────────────────────────────

    @Test
    void cancelPresence_promotesFirstFromQueue_whenWasConfirmed() {
        Match match = scheduledMatch(1);
        UUID matchId = match.getId();
        UUID waitingMemberId = UUID.randomUUID();

        PresenceEntry confirmedEntry = PresenceEntry.confirm(matchId, groupId, memberId);
        WaitingEntry waitingEntry = WaitingEntry.create(matchId, groupId, waitingMemberId, 1);

        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        when(matchRepository.findByIdAndGroupId(matchId, groupId)).thenReturn(Optional.of(match));
        when(matchRepository.findPresenceEntry(matchId, memberId)).thenReturn(Optional.of(confirmedEntry));
        when(matchRepository.findWaitingEntry(matchId, memberId)).thenReturn(Optional.empty());
        when(matchRepository.findFirstWaitingEntry(matchId)).thenReturn(Optional.of(waitingEntry));
        when(matchRepository.savePresenceEntry(any())).thenAnswer(inv -> inv.getArgument(0));

        matchService.execute(new CancelPresenceUseCase.Command(groupId, matchId, actorId));

        verify(matchRepository).deletePresenceEntry(confirmedEntry.getId());
        verify(matchRepository).deleteWaitingEntry(waitingEntry.getId());
        verify(matchRepository).savePresenceEntry(
                argThat(e -> e.getMemberId().equals(waitingMemberId) && e.getStatus() == PresenceStatus.CONFIRMED));
    }

    // ── Ban From Presence ─────────────────────────────────────────────────────

    @Test
    void banMember_throwsMemberAlreadyBanned_whenAlreadyBanned() {
        GroupMemberView banned = bannedView();
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        when(groupMemberPort.findMemberById(banned.id())).thenReturn(Optional.of(banned));

        assertThatThrownBy(() -> matchService.execute(
                new BanFromPresenceUseCase.Command(groupId, banned.id(), actorId, "Motivo")))
                .isInstanceOf(MemberAlreadyBannedException.class);
    }

    @Test
    void unbanMember_throwsMemberNotBanned_whenNotBanned() {
        GroupMemberView notBanned = playerView(UUID.randomUUID());
        when(groupMemberPort.findMember(groupId, actorId)).thenReturn(Optional.of(adminView()));
        when(groupMemberPort.findMemberById(notBanned.id())).thenReturn(Optional.of(notBanned));

        assertThatThrownBy(() -> matchService.execute(
                new UnbanFromPresenceUseCase.Command(groupId, notBanned.id(), actorId)))
                .isInstanceOf(MemberNotBannedException.class);
    }

    // ── Scheduler ─────────────────────────────────────────────────────────────

    @Test
    void closeExpiredLists_closesOnlyOpenListsPastCutoff() {
        Match openExpired = scheduledMatch(10);
        List<Match> toClose = List.of(openExpired);

        when(matchRepository.findByPresenceListStatusAndListClosesAtBefore(
                eq(PresenceListStatus.OPEN), any(Instant.class))).thenReturn(toClose);
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));

        com.arenahub.infrastructure.scheduler.PresenceListScheduler scheduler =
                new com.arenahub.infrastructure.scheduler.PresenceListScheduler(matchRepository);
        scheduler.closeExpiredLists();

        assertThat(openExpired.getPresenceListStatus()).isEqualTo(PresenceListStatus.CLOSED);
        verify(matchRepository).save(openExpired);
    }
}
