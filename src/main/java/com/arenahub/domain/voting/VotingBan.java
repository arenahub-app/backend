package com.arenahub.domain.voting;

import java.time.Instant;
import java.util.UUID;

public class VotingBan {

    private UUID id;
    private UUID votingId;
    private UUID groupId;
    private UUID memberId;
    private String reason;
    private UUID bannedBy;
    private Instant bannedAt;

    private VotingBan() {}

    public static VotingBan ban(UUID votingId, UUID groupId, UUID memberId, String reason, UUID bannedBy) {
        VotingBan b = new VotingBan();
        b.id = UUID.randomUUID();
        b.votingId = votingId;
        b.groupId = groupId;
        b.memberId = memberId;
        b.reason = reason;
        b.bannedBy = bannedBy;
        b.bannedAt = Instant.now();
        return b;
    }

    public static VotingBan reconstitute(UUID id, UUID votingId, UUID groupId,
                                         UUID memberId, String reason,
                                         UUID bannedBy, Instant bannedAt) {
        VotingBan b = new VotingBan();
        b.id = id;
        b.votingId = votingId;
        b.groupId = groupId;
        b.memberId = memberId;
        b.reason = reason;
        b.bannedBy = bannedBy;
        b.bannedAt = bannedAt;
        return b;
    }

    public UUID getId() { return id; }
    public UUID getVotingId() { return votingId; }
    public UUID getGroupId() { return groupId; }
    public UUID getMemberId() { return memberId; }
    public String getReason() { return reason; }
    public UUID getBannedBy() { return bannedBy; }
    public Instant getBannedAt() { return bannedAt; }
}
