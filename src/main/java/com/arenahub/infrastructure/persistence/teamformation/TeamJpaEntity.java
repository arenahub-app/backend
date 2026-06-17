package com.arenahub.infrastructure.persistence.teamformation;

import com.arenahub.domain.teamformation.Team;
import com.arenahub.domain.teamformation.TeamPlayer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
public class TeamJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "formation_id", nullable = false, columnDefinition = "uuid")
    private UUID formationId;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "average_skill", nullable = false, precision = 3, scale = 1)
    private BigDecimal averageSkill;

    @Column(name = "player_count", nullable = false)
    private int playerCount;

    public Team toDomain(List<TeamPlayer> players) {
        return Team.reconstitute(id, formationId, groupId, name, averageSkill, players);
    }

    public static TeamJpaEntity fromDomain(Team team) {
        TeamJpaEntity e = new TeamJpaEntity();
        e.id = team.getId();
        e.formationId = team.getFormationId();
        e.groupId = team.getGroupId();
        e.name = team.getName();
        e.averageSkill = team.getAverageSkill();
        e.playerCount = team.getPlayers().size();
        return e;
    }
}
