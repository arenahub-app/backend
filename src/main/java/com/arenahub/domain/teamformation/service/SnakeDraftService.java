package com.arenahub.domain.teamformation.service;

import com.arenahub.application.teamformation.port.out.MatchPresencePort.PlayerSnapshot;
import com.arenahub.domain.teamformation.Team;
import com.arenahub.domain.teamformation.TeamPlayer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SnakeDraftService {

    private static final BigDecimal STD_DEV_THRESHOLD = new BigDecimal("0.5");
    private static final int MAX_SWAP_ITERATIONS = 50;
    private static final String GOALKEEPER = "GOALKEEPER";

    public List<Team> distribute(List<PlayerSnapshot> players, int numberOfTeams, UUID groupId, UUID formationId) {
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < numberOfTeams; i++) {
            String name = "Time " + (char) ('A' + i);
            teams.add(Team.create(formationId, groupId, name));
        }

        // Goalkeepers are distributed separately and excluded from skill equalization
        List<PlayerSnapshot> goalkeepers = players.stream()
                .filter(p -> GOALKEEPER.equals(p.position()))
                .collect(Collectors.toList());
        List<PlayerSnapshot> outfield = players.stream()
                .filter(p -> !GOALKEEPER.equals(p.position()))
                .collect(Collectors.toList());

        Map<String, List<PlayerSnapshot>> byPosition = outfield.stream()
                .filter(p -> p.position() != null)
                .collect(Collectors.groupingBy(PlayerSnapshot::position));
        List<PlayerSnapshot> flex = outfield.stream()
                .filter(p -> p.position() == null)
                .collect(Collectors.toList());

        for (List<PlayerSnapshot> group : byPosition.values()) {
            snakeDraft(group, teams, groupId);
        }
        snakeDraft(flex, teams, groupId);

        if (calcOutfieldStdDev(teams).compareTo(STD_DEV_THRESHOLD) > 0) {
            trySwapsToEqualize(teams, groupId);
        }

        // Distribute goalkeepers round-robin after outfield balance is settled
        for (int i = 0; i < goalkeepers.size(); i++) {
            Team team = teams.get(i % teams.size());
            PlayerSnapshot gk = goalkeepers.get(i);
            team.addPlayer(TeamPlayer.create(team.getId(), groupId, gk.memberId(), gk.userName(), gk.skill(), gk.position()));
        }

        return teams;
    }

    private void snakeDraft(List<PlayerSnapshot> players, List<Team> teams, UUID groupId) {
        List<PlayerSnapshot> sorted = new ArrayList<>(players);
        sorted.sort((a, b) -> b.skill().compareTo(a.skill()));
        int n = teams.size();
        for (int i = 0; i < sorted.size(); i++) {
            int round = i / n;
            int posInRound = i % n;
            int teamIndex = (round % 2 == 0) ? posInRound : (n - 1 - posInRound);
            Team team = teams.get(teamIndex);
            PlayerSnapshot p = sorted.get(i);
            team.addPlayer(TeamPlayer.create(team.getId(), groupId, p.memberId(), p.userName(), p.skill(), p.position()));
        }
    }

    private BigDecimal calcOutfieldAvg(Team team) {
        List<TeamPlayer> outfield = team.getPlayers().stream()
                .filter(p -> !GOALKEEPER.equals(p.getPosition()))
                .collect(Collectors.toList());
        if (outfield.isEmpty()) return BigDecimal.ZERO;
        return outfield.stream()
                .map(TeamPlayer::getSkill)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(outfield.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcOutfieldStdDev(List<Team> teams) {
        if (teams.size() < 2) return BigDecimal.ZERO;
        List<BigDecimal> avgs = teams.stream().map(this::calcOutfieldAvg).collect(Collectors.toList());
        BigDecimal mean = avgs.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(teams.size()), 4, RoundingMode.HALF_UP);
        BigDecimal variance = avgs.stream()
                .map(avg -> avg.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(teams.size()), 4, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private void trySwapsToEqualize(List<Team> teams, UUID groupId) {
        for (int iter = 0; iter < MAX_SWAP_ITERATIONS; iter++) {
            boolean improved = false;
            BigDecimal currentDev = calcOutfieldStdDev(teams);
            for (int i = 0; i < teams.size() - 1; i++) {
                for (int j = i + 1; j < teams.size(); j++) {
                    Team ta = teams.get(i);
                    Team tb = teams.get(j);
                    for (TeamPlayer pa : new ArrayList<>(ta.getPlayers())) {
                        for (TeamPlayer pb : new ArrayList<>(tb.getPlayers())) {
                            if (GOALKEEPER.equals(pa.getPosition()) || GOALKEEPER.equals(pb.getPosition())) continue;
                            if (!Objects.equals(pa.getPosition(), pb.getPosition())) continue;
                            ta.removePlayer(pa.getMemberId());
                            tb.removePlayer(pb.getMemberId());
                            ta.addPlayer(TeamPlayer.create(ta.getId(), groupId, pb.getMemberId(), pb.getUserName(), pb.getSkill(), pb.getPosition()));
                            tb.addPlayer(TeamPlayer.create(tb.getId(), groupId, pa.getMemberId(), pa.getUserName(), pa.getSkill(), pa.getPosition()));
                            BigDecimal newDev = calcOutfieldStdDev(teams);
                            if (newDev.compareTo(currentDev) < 0) {
                                currentDev = newDev;
                                improved = true;
                                if (currentDev.compareTo(STD_DEV_THRESHOLD) <= 0) return;
                            } else {
                                ta.removePlayer(pb.getMemberId());
                                tb.removePlayer(pa.getMemberId());
                                ta.addPlayer(TeamPlayer.create(ta.getId(), groupId, pa.getMemberId(), pa.getUserName(), pa.getSkill(), pa.getPosition()));
                                tb.addPlayer(TeamPlayer.create(tb.getId(), groupId, pb.getMemberId(), pb.getUserName(), pb.getSkill(), pb.getPosition()));
                            }
                        }
                    }
                }
            }
            if (!improved) break;
        }
    }
}
