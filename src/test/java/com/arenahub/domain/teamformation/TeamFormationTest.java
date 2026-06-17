package com.arenahub.domain.teamformation;

import com.arenahub.domain.teamformation.vo.FormationType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TeamFormationTest {

    private final UUID matchId = UUID.randomUUID();
    private final UUID groupId = UUID.randomUUID();
    private final UUID confirmedBy = UUID.randomUUID();

    private TeamFormation createFormation(int numberOfTeams) {
        return TeamFormation.create(matchId, groupId, numberOfTeams, confirmedBy, List.of());
    }

    private Team createTeamWithPlayer(UUID formationId, String teamName, UUID memberId) {
        Team team = Team.create(formationId, groupId, teamName);
        TeamPlayer player = TeamPlayer.create(team.getId(), groupId, memberId, "Player", BigDecimal.valueOf(3.0), null);
        team.addPlayer(player);
        return team;
    }

    @Test
    void create_throwsWhenNumberOfTeamsLessThan2() {
        assertThatThrownBy(() -> createFormation(1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> createFormation(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_succeeds_withValidNumberOfTeams() {
        TeamFormation formation = createFormation(2);
        assertThat(formation.getId()).isNotNull();
        assertThat(formation.getFormationType()).isEqualTo(FormationType.AUTOMATIC);
        assertThat(formation.getNumberOfTeams()).isEqualTo(2);
    }

    @Test
    void movePlayer_updatesFormationTypeToManualAdjusted() {
        TeamFormation formation = createFormation(2);

        UUID member1 = UUID.randomUUID();
        UUID member2 = UUID.randomUUID();
        Team teamA = createTeamWithPlayer(formation.getId(), "Time A", member1);
        Team teamB = createTeamWithPlayer(formation.getId(), "Time B", member2);

        TeamFormation f = TeamFormation.reconstitute(formation.getId(), matchId, groupId, 2,
                FormationType.AUTOMATIC, confirmedBy, formation.getConfirmedAt(), List.of(teamA, teamB));

        f.movePlayer(member1, teamB.getId());

        assertThat(f.getFormationType()).isEqualTo(FormationType.MANUAL_ADJUSTED);
    }

    @Test
    void movePlayer_throwsWhenMemberNotInFormation() {
        TeamFormation formation = createFormation(2);
        UUID randomMember = UUID.randomUUID();
        UUID randomTeam = UUID.randomUUID();

        Team teamA = Team.create(formation.getId(), groupId, "Time A");
        Team teamB = Team.create(formation.getId(), groupId, "Time B");

        TeamFormation f = TeamFormation.reconstitute(formation.getId(), matchId, groupId, 2,
                FormationType.AUTOMATIC, confirmedBy, formation.getConfirmedAt(), List.of(teamA, teamB));

        assertThatThrownBy(() -> f.movePlayer(randomMember, randomTeam))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("player-not-in-formation");
    }

    @Test
    void movePlayer_throwsWhenToTeamIsAlreadyMembersTeam() {
        TeamFormation formation = createFormation(2);

        UUID member1 = UUID.randomUUID();
        Team teamA = createTeamWithPlayer(formation.getId(), "Time A", member1);
        Team teamB = Team.create(formation.getId(), groupId, "Time B");

        TeamFormation f = TeamFormation.reconstitute(formation.getId(), matchId, groupId, 2,
                FormationType.AUTOMATIC, confirmedBy, formation.getConfirmedAt(), List.of(teamA, teamB));

        assertThatThrownBy(() -> f.movePlayer(member1, teamA.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("player-already-in-team");
    }

    @Test
    void movePlayer_throwsWhenToTeamNotInFormation() {
        TeamFormation formation = createFormation(2);

        UUID member1 = UUID.randomUUID();
        Team teamA = createTeamWithPlayer(formation.getId(), "Time A", member1);
        Team teamB = Team.create(formation.getId(), groupId, "Time B");
        UUID unknownTeamId = UUID.randomUUID();

        TeamFormation f = TeamFormation.reconstitute(formation.getId(), matchId, groupId, 2,
                FormationType.AUTOMATIC, confirmedBy, formation.getConfirmedAt(), List.of(teamA, teamB));

        assertThatThrownBy(() -> f.movePlayer(member1, unknownTeamId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("team-not-in-formation");
    }
}
