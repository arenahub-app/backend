package com.arenahub.infrastructure.persistence.teamformation;

import com.arenahub.application.teamformation.port.out.MatchPresencePort;
import com.arenahub.application.teamformation.port.out.TeamFormationRepository;
import com.arenahub.domain.match.vo.GuestStatus;
import com.arenahub.domain.match.vo.PresenceStatus;
import com.arenahub.domain.teamformation.Team;
import com.arenahub.domain.teamformation.TeamFormation;
import com.arenahub.domain.teamformation.TeamPlayer;
import com.arenahub.infrastructure.persistence.group.GroupMemberJpaEntity;
import com.arenahub.infrastructure.persistence.group.GroupMemberJpaRepository;
import com.arenahub.infrastructure.persistence.match.MatchGuestJpaEntity;
import com.arenahub.infrastructure.persistence.match.MatchGuestJpaRepository;
import com.arenahub.infrastructure.persistence.match.MatchJpaEntity;
import com.arenahub.infrastructure.persistence.match.MatchJpaRepository;
import com.arenahub.infrastructure.persistence.match.PresenceEntryJpaEntity;
import com.arenahub.infrastructure.persistence.match.PresenceEntryJpaRepository;
import com.arenahub.infrastructure.persistence.user.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TeamFormationRepositoryAdapter implements TeamFormationRepository, MatchPresencePort {

    private final TeamFormationJpaRepository formationRepo;
    private final TeamJpaRepository teamRepo;
    private final TeamPlayerJpaRepository teamPlayerRepo;
    private final PresenceEntryJpaRepository presenceEntryRepo;
    private final GroupMemberJpaRepository memberRepo;
    private final UserJpaRepository userRepo;
    private final MatchJpaRepository matchRepo;
    private final MatchGuestJpaRepository guestRepo;

    public TeamFormationRepositoryAdapter(TeamFormationJpaRepository formationRepo,
                                          TeamJpaRepository teamRepo,
                                          TeamPlayerJpaRepository teamPlayerRepo,
                                          PresenceEntryJpaRepository presenceEntryRepo,
                                          GroupMemberJpaRepository memberRepo,
                                          UserJpaRepository userRepo,
                                          MatchJpaRepository matchRepo,
                                          MatchGuestJpaRepository guestRepo) {
        this.formationRepo = formationRepo;
        this.teamRepo = teamRepo;
        this.teamPlayerRepo = teamPlayerRepo;
        this.presenceEntryRepo = presenceEntryRepo;
        this.memberRepo = memberRepo;
        this.userRepo = userRepo;
        this.matchRepo = matchRepo;
        this.guestRepo = guestRepo;
    }

    @Override
    public TeamFormation save(TeamFormation formation) {
        formationRepo.save(TeamFormationJpaEntity.fromDomain(formation));

        List<TeamJpaEntity> existingTeams = teamRepo.findByFormationId(formation.getId());
        List<UUID> existingTeamIds = existingTeams.stream().map(TeamJpaEntity::getId).toList();
        if (!existingTeamIds.isEmpty()) {
            teamPlayerRepo.deleteByTeamIdIn(existingTeamIds);
        }

        for (Team team : formation.getTeams()) {
            teamRepo.save(TeamJpaEntity.fromDomain(team));
            for (TeamPlayer player : team.getPlayers()) {
                teamPlayerRepo.save(TeamPlayerJpaEntity.fromDomain(player));
            }
        }

        return reconstituteFormation(formationRepo.findById(formation.getId()).orElseThrow());
    }

    @Override
    public Optional<TeamFormation> findByMatchIdAndGroupId(UUID matchId, UUID groupId) {
        return formationRepo.findByMatchIdAndGroupId(matchId, groupId)
                .map(this::reconstituteFormation);
    }

    @Override
    public Optional<TeamFormation> findByIdAndGroupId(UUID id, UUID groupId) {
        return formationRepo.findByIdAndGroupId(id, groupId)
                .map(this::reconstituteFormation);
    }

    @Override
    public void deleteById(UUID id) {
        List<TeamJpaEntity> teams = teamRepo.findByFormationId(id);
        List<UUID> teamIds = teams.stream().map(TeamJpaEntity::getId).toList();
        if (!teamIds.isEmpty()) {
            teamPlayerRepo.deleteByTeamIdIn(teamIds);
            teamRepo.deleteByFormationId(id);
        }
        formationRepo.deleteById(id);
    }

    @Override
    public MatchPresenceSnapshot getPresenceSnapshot(UUID matchId, UUID groupId) {
        MatchJpaEntity match = matchRepo.findById(matchId)
                .orElseThrow(() -> new IllegalStateException("Match not found: " + matchId));

        boolean closed = match.getPresenceListStatus() ==
                com.arenahub.domain.match.vo.PresenceListStatus.CLOSED;

        List<PresenceEntryJpaEntity> entries =
                presenceEntryRepo.findByMatchIdAndStatus(matchId, PresenceStatus.CONFIRMED);

        List<PlayerSnapshot> players = new ArrayList<>();
        for (PresenceEntryJpaEntity entry : entries) {
            GroupMemberJpaEntity member = memberRepo.findById(entry.getMemberId()).orElse(null);
            if (member == null) continue;
            String userName = userRepo.findById(member.getUserId())
                    .map(u -> u.getName())
                    .orElse(null);
            String position = member.getPosition() != null ? member.getPosition().name() : null;
            String role = member.getRole() != null ? member.getRole().name() : null;
            players.add(new PlayerSnapshot(member.getId(), null, userName, member.getSkill(), position, role));
        }

        List<MatchGuestJpaEntity> guests = guestRepo.findByMatchIdAndStatus(matchId, GuestStatus.CONFIRMED);
        for (MatchGuestJpaEntity guest : guests) {
            String position = guest.getPosition() != null ? guest.getPosition().name() : null;
            players.add(new PlayerSnapshot(null, guest.getId(), guest.getName(), guest.getSkill(), position, null));
        }

        return new MatchPresenceSnapshot(closed, players);
    }

    private TeamFormation reconstituteFormation(TeamFormationJpaEntity entity) {
        List<TeamJpaEntity> teamEntities = teamRepo.findByFormationId(entity.getId());
        List<Team> teams = new ArrayList<>();
        for (TeamJpaEntity teamEntity : teamEntities) {
            List<TeamPlayerJpaEntity> playerEntities = teamPlayerRepo.findByTeamId(teamEntity.getId());
            List<TeamPlayer> players = new ArrayList<>();
            for (TeamPlayerJpaEntity playerEntity : playerEntities) {
                String userName = null;
                BigDecimal skill = BigDecimal.ZERO;
                if (playerEntity.getMemberId() != null) {
                    GroupMemberJpaEntity member = memberRepo.findById(playerEntity.getMemberId()).orElse(null);
                    if (member != null) {
                        skill = member.getSkill();
                        userName = userRepo.findById(member.getUserId())
                                .map(u -> u.getName())
                                .orElse(null);
                    }
                } else if (playerEntity.getGuestId() != null) {
                    MatchGuestJpaEntity guest = guestRepo.findById(playerEntity.getGuestId()).orElse(null);
                    if (guest != null) {
                        skill = guest.getSkill();
                        userName = guest.getName();
                    }
                }
                players.add(playerEntity.toDomain(userName, skill));
            }
            teams.add(teamEntity.toDomain(players));
        }
        return entity.toDomain(teams);
    }
}
