package com.arenahub.infrastructure.persistence.financial;

import com.arenahub.domain.financial.FinancialEntry;
import com.arenahub.domain.financial.vo.EntryCategory;
import com.arenahub.domain.financial.vo.EntryStatus;
import com.arenahub.domain.financial.vo.EntryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "financial_entries")
@Getter
@Setter
@NoArgsConstructor
public class FinancialEntryJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EntryType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EntryCategory category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "match_id", columnDefinition = "uuid")
    private UUID matchId;

    @Column(name = "reference_month", length = 7)
    private String referenceMonth;

    @Column(name = "source_charge_id", columnDefinition = "uuid")
    private UUID sourceChargeId;

    @Column(name = "registered_by", nullable = false, columnDefinition = "uuid")
    private UUID registeredBy;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EntryStatus status;

    @Column(name = "reversal_reason", columnDefinition = "TEXT")
    private String reversalReason;

    @Column(name = "reversed_by", columnDefinition = "uuid")
    private UUID reversedBy;

    @Column(name = "reversed_at")
    private Instant reversedAt;

    public static FinancialEntryJpaEntity fromDomain(FinancialEntry entry) {
        FinancialEntryJpaEntity e = new FinancialEntryJpaEntity();
        e.id = entry.getId();
        e.groupId = entry.getGroupId();
        e.type = entry.getType();
        e.category = entry.getCategory();
        e.amount = entry.getAmount();
        e.description = entry.getDescription();
        e.matchId = entry.getMatchId();
        e.referenceMonth = entry.getReferenceMonth();
        e.sourceChargeId = entry.getSourceChargeId();
        e.registeredBy = entry.getRegisteredBy();
        e.registeredAt = entry.getRegisteredAt();
        e.status = entry.getStatus();
        e.reversalReason = entry.getReversalReason();
        e.reversedBy = entry.getReversedBy();
        e.reversedAt = entry.getReversedAt();
        return e;
    }
}
