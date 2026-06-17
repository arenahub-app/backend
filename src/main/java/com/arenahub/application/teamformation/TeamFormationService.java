package com.arenahub.application.teamformation;

import com.arenahub.application.exception.*;
import com.arenahub.application.match.port.out.GroupMemberPort;
import com.arenahub.application.match.port.out.GroupMemberPort.GroupMemberView;
import com.arenahub.application.teamformation.port.in.*;
import com.arenahub.application.teamformation.port.out.MatchPresencePort;
import com.arenahub.application.teamformation.port.out.TeamFormationRepository;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.teamformation.Team;
import com.arenahub.domain.teamformation.TeamFormation;
import com.arenahub.domain.teamformation.service.SnakeDraftService;
import com.arenahub.infrastructure.persistence.group.GroupJpaRepository;
import com.arenahub.presentation.teamformation.dto.TeamFormationResponse;
import com.arenahub.presentation.teamformation.dto.TeamPlayerResponse;
import com.arenahub.presentation.teamformation.dto.TeamResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TeamFormationService implements
        GenerateTeamFormationUseCase, GetCurrentFormationUseCase,
        MovePlayerUseCase, DeleteFormationUseCase {

    private final TeamFormationRepository formationRepo;
    private final MatchPresencePort matchPresencePort;
    private final GroupMemberPort groupMemberPort;
    private final GroupJpaRepository groupRepo;
    private final SnakeDraftService snakeDraftService;

    public TeamFormationService(TeamFormationRepository formationRepo,
                                MatchPresencePort matchPresencePort,
                                GroupMemberPort groupMemberPort,
                                GroupJpaRepository groupRepo,
                                SnakeDraftService snakeDraftService) {
        this.formationRepo = formationRepo;
        this.matchPresencePort = matchPresencePort;
        this.groupMemberPort = groupMemberPort;
        this.groupRepo = groupRepo;
        this.snakeDraftService = snakeDraftService;
    }

    @Override
    public TeamFormationResponse execute(GenerateTeamFormationUseCase.Command cmd) {
        var group = groupRepo.findByIdAndDeletedAtIsNull(cmd.groupId())
                .orElseThrow(GroupNotFoundException::new);
        if (group.getStatus() != GroupStatus.ACTIVE) {
            throw new GroupInactiveException();
        }

        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);

        MatchPresencePort.MatchPresenceSnapshot snapshot =
                matchPresencePort.getPresenceSnapshot(cmd.matchId(), cmd.groupId());

        if (!snapshot.presenceListClosed()) {
            throw new PresenceListNotClosedException();
        }

        List<MatchPresencePort.PlayerSnapshot> players = snapshot.confirmedPlayers().stream()
                .filter(p -> !"REFEREE".equals(p.role()))
                .toList();

        if (players.size() < cmd.numberOfTeams()) {
            throw new NotEnoughPlayersException();
        }

        if (cmd.numberOfTeams() < 2) {
            throw new IllegalArgumentException("number-of-teams-invalid");
        }

        formationRepo.findByMatchIdAndGroupId(cmd.matchId(), cmd.groupId())
                .ifPresent(existing -> formationRepo.deleteById(existing.getId()));

        TeamFormation formation = TeamFormation.create(cmd.matchId(), cmd.groupId(),
                cmd.numberOfTeams(), actor.userId(), List.of());

        List<Team> teams = snakeDraftService.distribute(players, cmd.numberOfTeams(),
                cmd.groupId(), formation.getId());

        TeamFormation formationWithTeams = TeamFormation.reconstitute(
                formation.getId(), formation.getMatchId(), formation.getGroupId(),
                formation.getNumberOfTeams(), formation.getFormationType(),
                formation.getConfirmedBy(), formation.getConfirmedAt(), teams);

        TeamFormation saved = formationRepo.save(formationWithTeams);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamFormationResponse execute(GetCurrentFormationUseCase.Command cmd) {
        requireMember(cmd.groupId(), cmd.actorUserId());

        TeamFormation formation = formationRepo.findByMatchIdAndGroupId(cmd.matchId(), cmd.groupId())
                .orElseThrow(FormationNotFoundException::new);

        return toResponse(formation);
    }

    @Override
    public TeamFormationResponse execute(MovePlayerUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);

        TeamFormation formation = formationRepo.findByIdAndGroupId(cmd.formationId(), cmd.groupId())
                .orElseThrow(FormationNotFoundException::new);

        try {
            formation.movePlayer(cmd.memberId(), cmd.toTeamId());
        } catch (IllegalStateException ex) {
            switch (ex.getMessage()) {
                case "player-not-in-formation" -> throw new PlayerNotInFormationException();
                case "player-already-in-team" -> throw new PlayerAlreadyInTeamException();
                case "team-not-in-formation" -> throw new TeamNotInFormationException();
                case "cannot-empty-team" -> throw new CannotEmptyTeamException();
                default -> throw ex;
            }
        }

        TeamFormation saved = formationRepo.save(formation);
        return toResponse(saved);
    }

    @Override
    public void execute(DeleteFormationUseCase.Command cmd) {
        GroupMemberView actor = requireMember(cmd.groupId(), cmd.actorUserId());
        requireRole(actor, GroupRole.ADMIN);

        formationRepo.findByIdAndGroupId(cmd.formationId(), cmd.groupId())
                .orElseThrow(FormationNotFoundException::new);

        formationRepo.deleteById(cmd.formationId());
    }

    private GroupMemberView requireMember(UUID groupId, UUID userId) {
        return groupMemberPort.findMember(groupId, userId)
                .orElseThrow(NotAMemberException::new);
    }

    private void requireRole(GroupMemberView member, GroupRole required) {
        if (!member.role().isAtLeast(required)) throw new InsufficientRoleException();
    }

    private TeamFormationResponse toResponse(TeamFormation formation) {
        List<TeamResponse> teamResponses = formation.getTeams().stream()
                .map(team -> {
                    List<TeamPlayerResponse> playerResponses = team.getPlayers().stream()
                            .map(p -> new TeamPlayerResponse(p.getMemberId(), p.getUserName(),
                                    p.getSkill(), p.getPosition()))
                            .toList();
                    return new TeamResponse(team.getId(), team.getName(), team.getAverageSkill(),
                            team.getPlayers().size(), playerResponses);
                })
                .toList();

        return new TeamFormationResponse(
                formation.getId(), formation.getMatchId(), formation.getNumberOfTeams(),
                formation.getFormationType().name(), formation.getConfirmedBy(),
                formation.getConfirmedAt(), teamResponses);
    }
}
