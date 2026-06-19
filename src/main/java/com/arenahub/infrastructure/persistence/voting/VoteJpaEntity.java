package com.arenahub.infrastructure.persistence.voting;

import com.arenahub.domain.voting.Vote;
import com.arenahub.domain.voting.vo.Stars;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "votes")
@Getter
@Setter
@NoArgsConstructor
public class VoteJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "voting_id", nullable = false, columnDefinition = "uuid")
    private UUID votingId;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "voter_id", nullable = false, columnDefinition = "uuid")
    private UUID voterId;

    @Column(name = "target_member_id", nullable = false, columnDefinition = "uuid")
    private UUID targetMemberId;

    @Column(nullable = false)
    private int stars;

    @Column(name = "voted_at", nullable = false, updatable = false)
    private Instant votedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Vote toDomain() {
        return Vote.reconstitute(id, votingId, groupId, voterId, targetMemberId,
                new Stars(stars), votedAt, updatedAt);
    }

    public static VoteJpaEntity fromDomain(Vote vote) {
        VoteJpaEntity e = new VoteJpaEntity();
        e.id = vote.getId();
        e.votingId = vote.getVotingId();
        e.groupId = vote.getGroupId();
        e.voterId = vote.getVoterId();
        e.targetMemberId = vote.getTargetMemberId();
        e.stars = vote.getStars().value();
        e.votedAt = vote.getVotedAt();
        e.updatedAt = vote.getUpdatedAt();
        return e;
    }
}
