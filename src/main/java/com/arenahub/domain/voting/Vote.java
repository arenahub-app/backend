package com.arenahub.domain.voting;

import com.arenahub.domain.voting.vo.Stars;

import java.time.Instant;
import java.util.UUID;

public class Vote {

    private UUID id;
    private UUID votingId;
    private UUID groupId;
    private UUID voterId;
    private UUID targetMemberId;
    private Stars stars;
    private Instant votedAt;
    private Instant updatedAt;

    private Vote() {}

    public static Vote cast(UUID votingId, UUID groupId, UUID voterId, UUID targetMemberId, Stars stars) {
        if (voterId.equals(targetMemberId)) {
            throw new IllegalArgumentException("Jogador não pode votar em si mesmo");
        }
        Vote v = new Vote();
        v.id = UUID.randomUUID();
        v.votingId = votingId;
        v.groupId = groupId;
        v.voterId = voterId;
        v.targetMemberId = targetMemberId;
        v.stars = stars;
        v.votedAt = Instant.now();
        v.updatedAt = Instant.now();
        return v;
    }

    public static Vote reconstitute(UUID id, UUID votingId, UUID groupId,
                                    UUID voterId, UUID targetMemberId,
                                    Stars stars, Instant votedAt, Instant updatedAt) {
        Vote v = new Vote();
        v.id = id;
        v.votingId = votingId;
        v.groupId = groupId;
        v.voterId = voterId;
        v.targetMemberId = targetMemberId;
        v.stars = stars;
        v.votedAt = votedAt;
        v.updatedAt = updatedAt;
        return v;
    }

    public void update(Stars newStars) {
        this.stars = newStars;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getVotingId() { return votingId; }
    public UUID getGroupId() { return groupId; }
    public UUID getVoterId() { return voterId; }
    public UUID getTargetMemberId() { return targetMemberId; }
    public Stars getStars() { return stars; }
    public Instant getVotedAt() { return votedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
