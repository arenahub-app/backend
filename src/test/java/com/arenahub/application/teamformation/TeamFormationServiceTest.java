package com.arenahub.application.teamformation;

import com.arenahub.application.exception.*;
import com.arenahub.application.match.port.out.GroupMemberPort;
import com.arenahub.application.match.port.out.GroupMemberPort.GroupMemberView;
import com.arenahub.application.teamformation.port.in.GenerateTeamFormationUseCase;
import com.arenahub.application.teamformation.port.out.MatchPresencePort;
import com.arenahub.application.teamformation.port.out.TeamFormationRepository;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.group.vo.SkillSource;
import com.arenahub.domain.teamformation.Team;
import com.arenahub.domain.teamformation.TeamFormation;
import com.arenahub.domain.teamformation.service.SnakeDraftService;
import com.arenahub.infrastructure.persistence.group.GroupJpaEntity;
import com.arenahub.infrastructure.persistence.group.GroupJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamFormationServiceTest {

    @Mock
    TeamFormationRepository formationRepo;

    @Mock
    MatchPresencePort matchPresencePort;

    @Mock
    GroupMemberPort groupMemberPort;

    @Mock
    GroupJpaRepository groupRepo;

    @Mock
    SnakeDraftService snakeDraftService;

    private TeamFormationService service;

    private final UUID groupId = UUID.randomUUID();
    private final UUID matchId = UUID.randomUUID();
    private final UUID actorUserId = UUID.randomUUID();
    private final UUID actorMemberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new TeamFormationService(formationRepo, matchPresencePort, groupMemberPort,
                groupRepo, snakeDraftService);
    }

    private GroupMemberView adminView() {
        return new GroupMemberView(actorMemberId, actorUserId, groupId, "Admin",
                GroupRole.ADMIN, BigDecimal.valueOf(4.0), SkillSource.MANUAL, null,
                false, Instant.now(), false, null);
    }

    private GroupMemberView playerView() {
        return new GroupMemberView(UUID.randomUUID(), actorUserId, groupId, "Player",
                GroupRole.PLAYER, BigDecimal.valueOf(3.0), SkillSource.DEFAULT, null,
                false, Instant.now(), false, null);
    }

    private GroupJpaEntity activeGroup() {
        GroupJpaEntity g = new GroupJpaEntity();
        g.setStatus(GroupStatus.ACTIVE);
        return g;
    }

    private MatchPresencePort.PlayerSnapshot playerSnapshot(double skill, String role) {
        return new MatchPresencePort.PlayerSnapshot(UUID.randomUUID(), "Player", BigDecimal.valueOf(skill), null, role);
    }

    @Test
    void generate_throwsPresenceListNotClosed_whenListIsOpen() {
        when(groupRepo.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(activeGroup()));
        when(groupMemberPort.findMember(groupId, actorUserId)).thenReturn(Optional.of(adminView()));
        when(matchPresencePort.getPresenceSnapshot(matchId, groupId))
                .thenReturn(new MatchPresencePort.MatchPresenceSnapshot(false, List.of()));

        assertThatThrownBy(() -> service.execute(
                new GenerateTeamFormationUseCase.Command(groupId, matchId, actorUserId, 2)))
                .isInstanceOf(PresenceListNotClosedException.class);
    }

    @Test
    void generate_excludesReferees_fromDistribution() {
        when(groupRepo.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(activeGroup()));
        when(groupMemberPort.findMember(groupId, actorUserId)).thenReturn(Optional.of(adminView()));

        List<MatchPresencePort.PlayerSnapshot> players = List.of(
                playerSnapshot(5.0, "PLAYER"),
                playerSnapshot(4.0, "PLAYER"),
                playerSnapshot(3.0, "REFEREE")
        );
        when(matchPresencePort.getPresenceSnapshot(matchId, groupId))
                .thenReturn(new MatchPresencePort.MatchPresenceSnapshot(true, players));

        UUID formationId = UUID.randomUUID();
        List<Team> mockTeams = List.of(
                Team.create(formationId, groupId, "Time A"),
                Team.create(formationId, groupId, "Time B")
        );
        when(snakeDraftService.distribute(anyList(), eq(2), eq(groupId), any(UUID.class)))
                .thenReturn(mockTeams);
        when(formationRepo.findByMatchIdAndGroupId(matchId, groupId)).thenReturn(Optional.empty());
        when(formationRepo.save(any(TeamFormation.class))).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new GenerateTeamFormationUseCase.Command(groupId, matchId, actorUserId, 2));

        verify(snakeDraftService).distribute(
                argThat(list -> list.size() == 2 && list.stream().noneMatch(p -> "REFEREE".equals(p.role()))),
                eq(2), eq(groupId), any(UUID.class));
    }

    @Test
    void generate_throwsNotEnoughPlayers_whenPlayerCountLessThanTeams() {
        when(groupRepo.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(activeGroup()));
        when(groupMemberPort.findMember(groupId, actorUserId)).thenReturn(Optional.of(adminView()));

        List<MatchPresencePort.PlayerSnapshot> players = List.of(
                playerSnapshot(5.0, "PLAYER")
        );
        when(matchPresencePort.getPresenceSnapshot(matchId, groupId))
                .thenReturn(new MatchPresencePort.MatchPresenceSnapshot(true, players));

        assertThatThrownBy(() -> service.execute(
                new GenerateTeamFormationUseCase.Command(groupId, matchId, actorUserId, 2)))
                .isInstanceOf(NotEnoughPlayersException.class);
    }

    @Test
    void generate_deletesExistingFormation_beforeCreatingNew() {
        when(groupRepo.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(activeGroup()));
        when(groupMemberPort.findMember(groupId, actorUserId)).thenReturn(Optional.of(adminView()));

        List<MatchPresencePort.PlayerSnapshot> players = List.of(
                playerSnapshot(5.0, "PLAYER"),
                playerSnapshot(4.0, "PLAYER")
        );
        when(matchPresencePort.getPresenceSnapshot(matchId, groupId))
                .thenReturn(new MatchPresencePort.MatchPresenceSnapshot(true, players));

        UUID existingFormationId = UUID.randomUUID();
        TeamFormation existingFormation = TeamFormation.create(matchId, groupId, 2, actorMemberId, List.of());
        when(formationRepo.findByMatchIdAndGroupId(matchId, groupId))
                .thenReturn(Optional.of(existingFormation));

        List<Team> mockTeams = List.of(
                Team.create(existingFormationId, groupId, "Time A"),
                Team.create(existingFormationId, groupId, "Time B")
        );
        when(snakeDraftService.distribute(anyList(), eq(2), eq(groupId), any(UUID.class)))
                .thenReturn(mockTeams);
        when(formationRepo.save(any(TeamFormation.class))).thenAnswer(inv -> inv.getArgument(0));

        service.execute(new GenerateTeamFormationUseCase.Command(groupId, matchId, actorUserId, 2));

        verify(formationRepo).deleteById(existingFormation.getId());
    }

    @Test
    void generate_throwsInsufficientRole_whenPlayerTriesToGenerate() {
        when(groupRepo.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(activeGroup()));
        when(groupMemberPort.findMember(groupId, actorUserId)).thenReturn(Optional.of(playerView()));

        assertThatThrownBy(() -> service.execute(
                new GenerateTeamFormationUseCase.Command(groupId, matchId, actorUserId, 2)))
                .isInstanceOf(InsufficientRoleException.class);
    }
}
