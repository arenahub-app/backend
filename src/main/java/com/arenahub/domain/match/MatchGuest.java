package com.arenahub.domain.match;

import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.domain.match.vo.GuestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class MatchGuest {

    private UUID id;
    private UUID matchId;
    private UUID groupId;
    private String name;
    private BigDecimal skill;
    private PlayerPosition position;
    private GuestStatus status;
    private UUID addedBy;
    private Instant confirmedAt;
    private Instant createdAt;

    private MatchGuest() {}

    public static MatchGuest add(UUID matchId, UUID groupId, String name,
                                  BigDecimal skill, PlayerPosition position,
                                  UUID addedBy, boolean hasFee) {
        if (skill.compareTo(BigDecimal.ONE) < 0 || skill.compareTo(new BigDecimal("5.0")) > 0) {
            throw new IllegalArgumentException("invalid-skill");
        }
        MatchGuest g = new MatchGuest();
        g.id = UUID.randomUUID();
        g.matchId = matchId;
        g.groupId = groupId;
        g.name = name;
        g.skill = skill;
        g.position = position;
        g.addedBy = addedBy;
        g.status = hasFee ? GuestStatus.PAYMENT_PENDING : GuestStatus.CONFIRMED;
        g.confirmedAt = hasFee ? null : Instant.now();
        g.createdAt = Instant.now();
        return g;
    }

    public static MatchGuest reconstitute(UUID id, UUID matchId, UUID groupId, String name,
                                           BigDecimal skill, PlayerPosition position,
                                           GuestStatus status, UUID addedBy,
                                           Instant confirmedAt, Instant createdAt) {
        MatchGuest g = new MatchGuest();
        g.id = id;
        g.matchId = matchId;
        g.groupId = groupId;
        g.name = name;
        g.skill = skill;
        g.position = position;
        g.status = status;
        g.addedBy = addedBy;
        g.confirmedAt = confirmedAt;
        g.createdAt = createdAt;
        return g;
    }

    public void confirmAfterPayment() {
        this.status = GuestStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getMatchId() { return matchId; }
    public UUID getGroupId() { return groupId; }
    public String getName() { return name; }
    public BigDecimal getSkill() { return skill; }
    public PlayerPosition getPosition() { return position; }
    public GuestStatus getStatus() { return status; }
    public UUID getAddedBy() { return addedBy; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
