package com.arenahub.infrastructure.persistence.teamformation;

import com.arenahub.domain.teamformation.TeamFormation;
import com.arenahub.domain.teamformation.Team;
import com.arenahub.domain.teamformation.vo.FormationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "team_formations")
@Getter
@Setter
@NoArgsConstructor
public class TeamFormationJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "match_id", nullable = false, columnDefinition = "uuid")
    private UUID matchId;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "number_of_teams", nullable = false)
    private int numberOfTeams;

    @Column(name = "formation_type", nullable = false, length = 20)
    private String formationType;

    @Column(name = "confirmed_by", nullable = false, columnDefinition = "uuid")
    private UUID confirmedBy;

    @Column(name = "confirmed_at", nullable = false)
    private Instant confirmedAt;

    public TeamFormation toDomain(List<Team> teams) {
        return TeamFormation.reconstitute(id, matchId, groupId, numberOfTeams,
                FormationType.valueOf(formationType), confirmedBy, confirmedAt, teams);
    }

    public static TeamFormationJpaEntity fromDomain(TeamFormation formation) {
        TeamFormationJpaEntity e = new TeamFormationJpaEntity();
        e.id = formation.getId();
        e.matchId = formation.getMatchId();
        e.groupId = formation.getGroupId();
        e.numberOfTeams = formation.getNumberOfTeams();
        e.formationType = formation.getFormationType().name();
        e.confirmedBy = formation.getConfirmedBy();
        e.confirmedAt = formation.getConfirmedAt();
        return e;
    }
}
