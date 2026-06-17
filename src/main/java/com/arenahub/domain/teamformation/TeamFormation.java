package com.arenahub.domain.teamformation;

import com.arenahub.domain.teamformation.vo.FormationType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamFormation {

    private UUID id;
    private UUID matchId;
    private UUID groupId;
    private int numberOfTeams;
    private FormationType formationType;
    private UUID confirmedBy;
    private Instant confirmedAt;
    private List<Team> teams;

    private TeamFormation() {}

    public static TeamFormation create(UUID matchId, UUID groupId, int numberOfTeams,
                                       UUID confirmedBy, List<Team> teams) {
        if (numberOfTeams < 2) {
            throw new IllegalArgumentException("number-of-teams-invalid");
        }
        TeamFormation f = new TeamFormation();
        f.id = UUID.randomUUID();
        f.matchId = matchId;
        f.groupId = groupId;
        f.numberOfTeams = numberOfTeams;
        f.formationType = FormationType.AUTOMATIC;
        f.confirmedBy = confirmedBy;
        f.confirmedAt = Instant.now();
        f.teams = new ArrayList<>(teams);
        return f;
    }

    public static TeamFormation reconstitute(UUID id, UUID matchId, UUID groupId, int numberOfTeams,
                                             FormationType formationType, UUID confirmedBy,
                                             Instant confirmedAt, List<Team> teams) {
        TeamFormation f = new TeamFormation();
        f.id = id;
        f.matchId = matchId;
        f.groupId = groupId;
        f.numberOfTeams = numberOfTeams;
        f.formationType = formationType;
        f.confirmedBy = confirmedBy;
        f.confirmedAt = confirmedAt;
        f.teams = new ArrayList<>(teams);
        return f;
    }

    public void movePlayer(UUID memberId, UUID toTeamId) {
        Team fromTeam = null;
        TeamPlayer movingPlayer = null;
        for (Team team : teams) {
            for (TeamPlayer player : team.getPlayers()) {
                if (player.getMemberId().equals(memberId)) {
                    fromTeam = team;
                    movingPlayer = player;
                    break;
                }
            }
            if (fromTeam != null) break;
        }

        if (fromTeam == null) {
            throw new IllegalStateException("player-not-in-formation");
        }

        if (fromTeam.getId().equals(toTeamId)) {
            throw new IllegalStateException("player-already-in-team");
        }

        Team toTeam = teams.stream()
                .filter(t -> t.getId().equals(toTeamId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("team-not-in-formation"));

        fromTeam.removePlayer(memberId);
        toTeam.addPlayer(TeamPlayer.create(toTeamId, movingPlayer.getGroupId(),
                movingPlayer.getMemberId(), movingPlayer.getUserName(),
                movingPlayer.getSkill(), movingPlayer.getPosition()));

        this.formationType = FormationType.MANUAL_ADJUSTED;
    }

    public UUID getId() { return id; }
    public UUID getMatchId() { return matchId; }
    public UUID getGroupId() { return groupId; }
    public int getNumberOfTeams() { return numberOfTeams; }
    public FormationType getFormationType() { return formationType; }
    public UUID getConfirmedBy() { return confirmedBy; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public List<Team> getTeams() { return teams; }
}
