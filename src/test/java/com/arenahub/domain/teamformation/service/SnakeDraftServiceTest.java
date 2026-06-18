package com.arenahub.domain.teamformation.service;

import com.arenahub.application.teamformation.port.out.MatchPresencePort.PlayerSnapshot;
import com.arenahub.domain.teamformation.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SnakeDraftServiceTest {

    private SnakeDraftService snakeDraftService;
    private final UUID groupId = UUID.randomUUID();
    private final UUID formationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        snakeDraftService = new SnakeDraftService();
    }

    private PlayerSnapshot player(String name, double skill) {
        return new PlayerSnapshot(UUID.randomUUID(), null, name, BigDecimal.valueOf(skill), null, "PLAYER");
    }

    private PlayerSnapshot playerWithPosition(String name, double skill, String position) {
        return new PlayerSnapshot(UUID.randomUUID(), null, name, BigDecimal.valueOf(skill), position, "PLAYER");
    }

    @Test
    void distribute_10players_2teams_each5Players() {
        List<PlayerSnapshot> players = List.of(
                player("P1", 6.0), player("P2", 5.5), player("P3", 5.0), player("P4", 4.5),
                player("P5", 4.0), player("P6", 3.5), player("P7", 3.0), player("P8", 2.5),
                player("P9", 2.0), player("P10", 1.5)
        );

        List<Team> teams = snakeDraftService.distribute(players, 2, groupId, formationId);

        assertThat(teams).hasSize(2);
        assertThat(teams.get(0).getPlayers()).hasSize(5);
        assertThat(teams.get(1).getPlayers()).hasSize(5);

        BigDecimal diff = teams.get(0).getAverageSkill().subtract(teams.get(1).getAverageSkill()).abs();
        assertThat(diff.compareTo(BigDecimal.valueOf(1.0))).isLessThanOrEqualTo(0);
    }

    @Test
    void distribute_9players_3teams_each3Players() {
        List<PlayerSnapshot> players = List.of(
                player("P1", 6.0), player("P2", 5.0), player("P3", 4.0),
                player("P4", 4.0), player("P5", 3.0), player("P6", 3.0),
                player("P7", 2.0), player("P8", 2.0), player("P9", 1.0)
        );

        List<Team> teams = snakeDraftService.distribute(players, 3, groupId, formationId);

        assertThat(teams).hasSize(3);
        teams.forEach(t -> assertThat(t.getPlayers()).hasSize(3));
    }

    @Test
    void distribute_emptyList_doesNotThrow() {
        List<Team> teams = snakeDraftService.distribute(List.of(), 2, groupId, formationId);

        assertThat(teams).hasSize(2);
        teams.forEach(t -> assertThat(t.getPlayers()).isEmpty());
    }

    @Test
    void distribute_withPositions_eachTeamGetsEqualPositionCount() {
        List<PlayerSnapshot> players = List.of(
                playerWithPosition("GK1", 5.5, "GOALKEEPER"),
                playerWithPosition("GK2", 3.0, "GOALKEEPER"),
                playerWithPosition("FW1", 5.0, "FORWARD"),
                playerWithPosition("FW2", 3.0, "FORWARD"),
                player("Flex1", 4.0),
                player("Flex2", 3.0)
        );

        List<Team> teams = snakeDraftService.distribute(players, 2, groupId, formationId);

        assertThat(teams).hasSize(2);
        assertThat(teams.get(0).getPlayers()).hasSize(3);
        assertThat(teams.get(1).getPlayers()).hasSize(3);

        long gkA = teams.get(0).getPlayers().stream().filter(p -> "GOALKEEPER".equals(p.getPosition())).count();
        long gkB = teams.get(1).getPlayers().stream().filter(p -> "GOALKEEPER".equals(p.getPosition())).count();
        assertThat(gkA).isEqualTo(1);
        assertThat(gkB).isEqualTo(1);

        long fwA = teams.get(0).getPlayers().stream().filter(p -> "FORWARD".equals(p.getPosition())).count();
        long fwB = teams.get(1).getPlayers().stream().filter(p -> "FORWARD".equals(p.getPosition())).count();
        assertThat(fwA).isEqualTo(1);
        assertThat(fwB).isEqualTo(1);
    }

    @Test
    void distribute_goalkeepersExcludedFromSkillEqualization() {
        // GKs with very different skills should not affect which team has better outfield average
        List<PlayerSnapshot> players = List.of(
                playerWithPosition("GK_Elite", 6.0, "GOALKEEPER"),
                playerWithPosition("GK_Weak",  1.0, "GOALKEEPER"),
                playerWithPosition("FW_A", 5.0, "FORWARD"),
                playerWithPosition("FW_B", 5.0, "FORWARD"),
                player("Mid_A", 4.0),
                player("Mid_B", 4.0)
        );

        List<Team> teams = snakeDraftService.distribute(players, 2, groupId, formationId);

        // Each team must have exactly 1 GK
        long gkA = teams.get(0).getPlayers().stream().filter(p -> "GOALKEEPER".equals(p.getPosition())).count();
        long gkB = teams.get(1).getPlayers().stream().filter(p -> "GOALKEEPER".equals(p.getPosition())).count();
        assertThat(gkA).isEqualTo(1);
        assertThat(gkB).isEqualTo(1);

        // Outfield average must be equal (4.5 each), regardless of GK skill difference
        BigDecimal outfieldAvgA = teams.get(0).getPlayers().stream()
                .filter(p -> !"GOALKEEPER".equals(p.getPosition()))
                .map(p -> p.getSkill())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(2), 4, java.math.RoundingMode.HALF_UP);
        BigDecimal outfieldAvgB = teams.get(1).getPlayers().stream()
                .filter(p -> !"GOALKEEPER".equals(p.getPosition()))
                .map(p -> p.getSkill())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(2), 4, java.math.RoundingMode.HALF_UP);
        assertThat(outfieldAvgA).isEqualByComparingTo(outfieldAvgB);
    }

    @Test
    void distribute_unbalancedSkills_attemptsEqualization() {
        List<PlayerSnapshot> players = new ArrayList<>();
        players.add(player("P1", 6.0));
        players.add(player("P2", 6.0));
        players.add(player("P3", 6.0));
        players.add(player("P4", 6.0));
        players.add(player("P5", 1.0));
        players.add(player("P6", 1.0));
        players.add(player("P7", 1.0));
        players.add(player("P8", 1.0));

        List<Team> teams = snakeDraftService.distribute(players, 2, groupId, formationId);

        assertThat(teams).hasSize(2);
        assertThat(teams.get(0).getPlayers()).hasSize(4);
        assertThat(teams.get(1).getPlayers()).hasSize(4);

        BigDecimal avgA = teams.get(0).getAverageSkill();
        BigDecimal avgB = teams.get(1).getAverageSkill();
        assertThat(avgA).isEqualByComparingTo(avgB);
    }
}
