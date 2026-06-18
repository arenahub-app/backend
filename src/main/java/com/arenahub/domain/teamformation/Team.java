package com.arenahub.domain.teamformation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {

    private UUID id;
    private UUID formationId;
    private UUID groupId;
    private String name;
    private BigDecimal averageSkill;
    private List<TeamPlayer> players;

    private Team() {}

    public static Team create(UUID formationId, UUID groupId, String name) {
        Team t = new Team();
        t.id = UUID.randomUUID();
        t.formationId = formationId;
        t.groupId = groupId;
        t.name = name;
        t.averageSkill = BigDecimal.ZERO;
        t.players = new ArrayList<>();
        return t;
    }

    public static Team reconstitute(UUID id, UUID formationId, UUID groupId, String name,
                                    BigDecimal averageSkill, List<TeamPlayer> players) {
        Team t = new Team();
        t.id = id;
        t.formationId = formationId;
        t.groupId = groupId;
        t.name = name;
        t.averageSkill = averageSkill;
        t.players = new ArrayList<>(players);
        return t;
    }

    public void addPlayer(TeamPlayer player) {
        players.add(player);
        recalculateAverageSkill();
    }

    public void removePlayer(UUID memberId) {
        boolean removed = players.removeIf(p -> memberId.equals(p.getMemberId()));
        if (!removed) {
            throw new IllegalStateException("player-not-in-team");
        }
        recalculateAverageSkill();
    }

    public void removeGuestPlayer(UUID guestId) {
        boolean removed = players.removeIf(p -> guestId.equals(p.getGuestId()));
        if (!removed) {
            throw new IllegalStateException("player-not-in-team");
        }
        recalculateAverageSkill();
    }

    public void recalculateAverageSkill() {
        if (players.isEmpty()) {
            averageSkill = BigDecimal.ZERO;
            return;
        }
        BigDecimal sum = players.stream()
                .map(TeamPlayer::getSkill)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        averageSkill = sum.divide(BigDecimal.valueOf(players.size()), 1, RoundingMode.HALF_UP);
    }

    public UUID getId() { return id; }
    public UUID getFormationId() { return formationId; }
    public UUID getGroupId() { return groupId; }
    public String getName() { return name; }
    public BigDecimal getAverageSkill() { return averageSkill; }
    public List<TeamPlayer> getPlayers() { return players; }
}
