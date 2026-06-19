package com.arenahub.infrastructure.persistence.voting;

import com.arenahub.domain.voting.SkillVoting;
import com.arenahub.domain.voting.vo.VotingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "skill_votings")
@Getter
@Setter
@NoArgsConstructor
public class SkillVotingJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VotingStatus status;

    @Column(name = "opened_by", nullable = false, columnDefinition = "uuid")
    private UUID openedBy;

    @Column(name = "opened_at", nullable = false, updatable = false)
    private Instant openedAt;

    @Column(nullable = false)
    private Instant deadline;

    @Column(name = "closed_at")
    private Instant closedAt;

    public SkillVoting toDomain() {
        return SkillVoting.reconstitute(id, groupId, status, openedBy, openedAt, deadline, closedAt);
    }

    public static SkillVotingJpaEntity fromDomain(SkillVoting sv) {
        SkillVotingJpaEntity e = new SkillVotingJpaEntity();
        e.id = sv.getId();
        e.groupId = sv.getGroupId();
        e.status = sv.getStatus();
        e.openedBy = sv.getOpenedBy();
        e.openedAt = sv.getOpenedAt();
        e.deadline = sv.getDeadline();
        e.closedAt = sv.getClosedAt();
        return e;
    }
}
