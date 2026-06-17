package com.arenahub.domain.financial;

import com.arenahub.domain.financial.vo.EntryCategory;
import com.arenahub.domain.financial.vo.EntryStatus;
import com.arenahub.domain.financial.vo.EntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class FinancialEntry {

    private UUID id;
    private UUID groupId;
    private EntryType type;
    private EntryCategory category;
    private BigDecimal amount;
    private String description;
    private UUID matchId;
    private String referenceMonth;
    private UUID sourceChargeId;
    private UUID registeredBy;
    private Instant registeredAt;
    private EntryStatus status;
    private String reversalReason;
    private UUID reversedBy;
    private Instant reversedAt;

    private FinancialEntry() {}

    public static FinancialEntry createRevenue(UUID groupId, EntryCategory category,
                                                BigDecimal amount, String description,
                                                UUID matchId, UUID sourceChargeId,
                                                UUID registeredBy) {
        FinancialEntry e = new FinancialEntry();
        e.id = UUID.randomUUID();
        e.groupId = groupId;
        e.type = EntryType.REVENUE;
        e.category = category;
        e.amount = amount;
        e.description = description;
        e.matchId = matchId;
        e.sourceChargeId = sourceChargeId;
        e.registeredBy = registeredBy;
        e.registeredAt = Instant.now();
        e.status = EntryStatus.ACTIVE;
        return e;
    }

    public UUID getId() { return id; }
    public UUID getGroupId() { return groupId; }
    public EntryType getType() { return type; }
    public EntryCategory getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public UUID getMatchId() { return matchId; }
    public String getReferenceMonth() { return referenceMonth; }
    public UUID getSourceChargeId() { return sourceChargeId; }
    public UUID getRegisteredBy() { return registeredBy; }
    public Instant getRegisteredAt() { return registeredAt; }
    public EntryStatus getStatus() { return status; }
    public String getReversalReason() { return reversalReason; }
    public UUID getReversedBy() { return reversedBy; }
    public Instant getReversedAt() { return reversedAt; }
}
