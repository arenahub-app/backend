package com.arenahub.domain.teamformation.service;

import com.arenahub.application.teamformation.port.out.MatchPresencePort.PlayerSnapshot;
import com.arenahub.domain.teamformation.Team;
import com.arenahub.domain.teamformation.TeamPlayer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class SnakeDraftService {

    private static final BigDecimal STD_DEV_THRESHOLD = new BigDecimal("0.5");
    private static final int MAX_SWAP_ITERATIONS = 50;

    public List<Team> distribute(List<PlayerSnapshot> players, int numberOfTeams, UUID groupId, UUID formationId) {
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < numberOfTeams; i++) {
            String name = "Time " + (char) ('A' + i);
            teams.add(Team.create(formationId, groupId, name));
        }

        List<PlayerSnapshot> sorted = new ArrayList<>(players);
        sorted.sort((a, b) -> b.skill().compareTo(a.skill()));

        for (int i = 0; i < sorted.size(); i++) {
            int round = i / numberOfTeams;
            int posInRound = i % numberOfTeams;
            int teamIndex = (round % 2 == 0) ? posInRound : (numberOfTeams - 1 - posInRound);
            Team team = teams.get(teamIndex);
            PlayerSnapshot p = sorted.get(i);
            team.addPlayer(TeamPlayer.create(team.getId(), groupId, p.memberId(), p.userName(), p.skill(), p.position()));
        }

        if (calcStdDev(teams).compareTo(STD_DEV_THRESHOLD) > 0) {
            trySwapsToEqualize(teams, groupId);
        }

        return teams;
    }

    private BigDecimal calcStdDev(List<Team> teams) {
        if (teams.size() < 2) return BigDecimal.ZERO;
        BigDecimal sum = teams.stream()
                .map(Team::getAverageSkill)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mean = sum.divide(BigDecimal.valueOf(teams.size()), 4, RoundingMode.HALF_UP);
        BigDecimal variance = teams.stream()
                .map(t -> t.getAverageSkill().subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(teams.size()), 4, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private void trySwapsToEqualize(List<Team> teams, UUID groupId) {
        for (int iter = 0; iter < MAX_SWAP_ITERATIONS; iter++) {
            boolean improved = false;
            BigDecimal currentDev = calcStdDev(teams);
            for (int i = 0; i < teams.size() - 1; i++) {
                for (int j = i + 1; j < teams.size(); j++) {
                    Team ta = teams.get(i);
                    Team tb = teams.get(j);
                    for (TeamPlayer pa : new ArrayList<>(ta.getPlayers())) {
                        for (TeamPlayer pb : new ArrayList<>(tb.getPlayers())) {
                            ta.removePlayer(pa.getMemberId());
                            tb.removePlayer(pb.getMemberId());
                            ta.addPlayer(TeamPlayer.create(ta.getId(), groupId, pb.getMemberId(), pb.getUserName(), pb.getSkill(), pb.getPosition()));
                            tb.addPlayer(TeamPlayer.create(tb.getId(), groupId, pa.getMemberId(), pa.getUserName(), pa.getSkill(), pa.getPosition()));
                            BigDecimal newDev = calcStdDev(teams);
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
