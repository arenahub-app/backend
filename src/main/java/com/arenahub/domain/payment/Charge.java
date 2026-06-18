package com.arenahub.domain.payment;

import com.arenahub.domain.payment.vo.ChargeStatus;
import com.arenahub.domain.payment.vo.ChargeType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Charge {

    private UUID id;
    private UUID groupId;
    private UUID memberId;
    private UUID guestId;
    private ChargeType type;
    private BigDecimal amount;
    private UUID referenceMatchId;
    private String referenceMonth;
    private ChargeStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private Charge() {}

    public static Charge createDaily(UUID groupId, UUID memberId,
                                      BigDecimal amount, UUID matchId) {
        Charge c = new Charge();
        c.id = UUID.randomUUID();
        c.groupId = groupId;
        c.memberId = memberId;
        c.guestId = null;
        c.type = ChargeType.DAILY;
        c.amount = amount;
        c.referenceMatchId = matchId;
        c.status = ChargeStatus.PENDING;
        c.createdAt = Instant.now();
        c.updatedAt = Instant.now();
        return c;
    }

    public static Charge createDailyForGuest(UUID groupId, UUID guestId,
                                              BigDecimal amount, UUID matchId) {
        Charge c = new Charge();
        c.id = UUID.randomUUID();
        c.groupId = groupId;
        c.memberId = null;
        c.guestId = guestId;
        c.type = ChargeType.DAILY;
        c.amount = amount;
        c.referenceMatchId = matchId;
        c.status = ChargeStatus.PENDING;
        c.createdAt = Instant.now();
        c.updatedAt = Instant.now();
        return c;
    }

    public static Charge reconstitute(UUID id, UUID groupId, UUID memberId, UUID guestId,
                                       ChargeType type, BigDecimal amount,
                                       UUID referenceMatchId, String referenceMonth,
                                       ChargeStatus status,
                                       Instant createdAt, Instant updatedAt) {
        Charge c = new Charge();
        c.id = id;
        c.groupId = groupId;
        c.memberId = memberId;
        c.guestId = guestId;
        c.type = type;
        c.amount = amount;
        c.referenceMatchId = referenceMatchId;
        c.referenceMonth = referenceMonth;
        c.status = status;
        c.createdAt = createdAt;
        c.updatedAt = updatedAt;
        return c;
    }

    public void approve() {
        if (status == ChargeStatus.APPROVED) {
            throw new IllegalStateException("charge-already-approved");
        }
        this.status = ChargeStatus.APPROVED;
        this.updatedAt = Instant.now();
    }

    public boolean isPending() {
        return status == ChargeStatus.PENDING;
    }

    public boolean isGuestCharge() {
        return guestId != null;
    }

    public UUID getId() { return id; }
    public UUID getGroupId() { return groupId; }
    public UUID getMemberId() { return memberId; }
    public UUID getGuestId() { return guestId; }
    public ChargeType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public UUID getReferenceMatchId() { return referenceMatchId; }
    public String getReferenceMonth() { return referenceMonth; }
    public ChargeStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
