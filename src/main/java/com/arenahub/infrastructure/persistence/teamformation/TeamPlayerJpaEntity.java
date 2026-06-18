package com.arenahub.infrastructure.persistence.teamformation;

import com.arenahub.domain.teamformation.TeamPlayer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "team_players")
@Getter
@Setter
@NoArgsConstructor
public class TeamPlayerJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "team_id", nullable = false, columnDefinition = "uuid")
    private UUID teamId;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "member_id", columnDefinition = "uuid")
    private UUID memberId;

    @Column(name = "guest_id", columnDefinition = "uuid")
    private UUID guestId;

    @Column(length = 20)
    private String position;

    public TeamPlayer toDomain(String userName, BigDecimal skill) {
        return TeamPlayer.reconstitute(id, teamId, groupId, memberId, guestId, userName, skill, position);
    }

    public static TeamPlayerJpaEntity fromDomain(TeamPlayer player) {
        TeamPlayerJpaEntity e = new TeamPlayerJpaEntity();
        e.id = player.getId();
        e.teamId = player.getTeamId();
        e.groupId = player.getGroupId();
        e.memberId = player.getMemberId();
        e.guestId = player.getGuestId();
        e.position = player.getPosition();
        return e;
    }
}
