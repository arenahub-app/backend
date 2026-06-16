package com.arenahub.domain.match;

import java.time.Instant;
import java.util.UUID;

public class WaitingEntry {

    private UUID id;
    private UUID matchId;
    private UUID groupId;
    private UUID memberId;
    private int position;
    private Instant notifiedAt;
    private Instant notificationDeadline;
    private Instant createdAt;

    private WaitingEntry() {}

    public static WaitingEntry create(UUID matchId, UUID groupId, UUID memberId, int nextPosition) {
        WaitingEntry e = new WaitingEntry();
        e.id = UUID.randomUUID();
        e.matchId = matchId;
        e.groupId = groupId;
        e.memberId = memberId;
        e.position = nextPosition;
        e.createdAt = Instant.now();
        return e;
    }

    public static WaitingEntry reconstitute(UUID id, UUID matchId, UUID groupId, UUID memberId,
                                             int position, Instant notifiedAt,
                                             Instant notificationDeadline, Instant createdAt) {
        WaitingEntry e = new WaitingEntry();
        e.id = id;
        e.matchId = matchId;
        e.groupId = groupId;
        e.memberId = memberId;
        e.position = position;
        e.notifiedAt = notifiedAt;
        e.notificationDeadline = notificationDeadline;
        e.createdAt = createdAt;
        return e;
    }

    public UUID getId() { return id; }
    public UUID getMatchId() { return matchId; }
    public UUID getGroupId() { return groupId; }
    public UUID getMemberId() { return memberId; }
    public int getPosition() { return position; }
    public Instant getNotifiedAt() { return notifiedAt; }
    public Instant getNotificationDeadline() { return notificationDeadline; }
    public Instant getCreatedAt() { return createdAt; }
}
