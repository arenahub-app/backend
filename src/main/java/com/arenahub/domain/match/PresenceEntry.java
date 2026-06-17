package com.arenahub.domain.match;

import com.arenahub.domain.match.vo.JustificationStatus;
import com.arenahub.domain.match.vo.PresenceStatus;

import java.time.Instant;
import java.util.UUID;

public class PresenceEntry {

    private UUID id;
    private UUID matchId;
    private UUID groupId;
    private UUID memberId;
    private PresenceStatus status;
    private String justification;
    private JustificationStatus justificationStatus;
    private Instant confirmedAt;

    private PresenceEntry() {}

    public static PresenceEntry confirm(UUID matchId, UUID groupId, UUID memberId) {
        PresenceEntry e = new PresenceEntry();
        e.id = UUID.randomUUID();
        e.matchId = matchId;
        e.groupId = groupId;
        e.memberId = memberId;
        e.status = PresenceStatus.CONFIRMED;
        e.confirmedAt = Instant.now();
        return e;
    }

    public static PresenceEntry awaitingPayment(UUID matchId, UUID groupId, UUID memberId) {
        PresenceEntry e = new PresenceEntry();
        e.id = UUID.randomUUID();
        e.matchId = matchId;
        e.groupId = groupId;
        e.memberId = memberId;
        e.status = PresenceStatus.PAYMENT_PENDING;
        return e;
    }

    public void confirmAfterPayment() {
        this.status = PresenceStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
    }

    public static PresenceEntry decline(UUID matchId, UUID groupId, UUID memberId) {
        PresenceEntry e = new PresenceEntry();
        e.id = UUID.randomUUID();
        e.matchId = matchId;
        e.groupId = groupId;
        e.memberId = memberId;
        e.status = PresenceStatus.DECLINED;
        return e;
    }

    public static PresenceEntry pendingBan(UUID matchId, UUID groupId, UUID memberId, String banReason) {
        PresenceEntry e = new PresenceEntry();
        e.id = UUID.randomUUID();
        e.matchId = matchId;
        e.groupId = groupId;
        e.memberId = memberId;
        e.status = PresenceStatus.BANNED_PENDING;
        e.justification = banReason;
        e.justificationStatus = JustificationStatus.PENDING;
        return e;
    }

    public static PresenceEntry reconstitute(UUID id, UUID matchId, UUID groupId, UUID memberId,
                                              PresenceStatus status, String justification,
                                              JustificationStatus justificationStatus,
                                              Instant confirmedAt) {
        PresenceEntry e = new PresenceEntry();
        e.id = id;
        e.matchId = matchId;
        e.groupId = groupId;
        e.memberId = memberId;
        e.status = status;
        e.justification = justification;
        e.justificationStatus = justificationStatus;
        e.confirmedAt = confirmedAt;
        return e;
    }

    public UUID getId() { return id; }
    public UUID getMatchId() { return matchId; }
    public UUID getGroupId() { return groupId; }
    public UUID getMemberId() { return memberId; }
    public PresenceStatus getStatus() { return status; }
    public String getJustification() { return justification; }
    public JustificationStatus getJustificationStatus() { return justificationStatus; }
    public Instant getConfirmedAt() { return confirmedAt; }
}
