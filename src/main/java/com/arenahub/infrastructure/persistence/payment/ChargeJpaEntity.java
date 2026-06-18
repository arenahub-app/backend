package com.arenahub.infrastructure.persistence.payment;

import com.arenahub.domain.payment.Charge;
import com.arenahub.domain.payment.vo.ChargeStatus;
import com.arenahub.domain.payment.vo.ChargeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "charges")
@Getter
@Setter
@NoArgsConstructor
public class ChargeJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "member_id", columnDefinition = "uuid")
    private UUID memberId;

    @Column(name = "guest_id", columnDefinition = "uuid")
    private UUID guestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ChargeType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "reference_match_id", columnDefinition = "uuid")
    private UUID referenceMatchId;

    @Column(name = "reference_month", length = 7)
    private String referenceMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ChargeStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Charge toDomain() {
        return Charge.reconstitute(id, groupId, memberId, guestId, type, amount,
                referenceMatchId, referenceMonth, status, createdAt, updatedAt);
    }

    public static ChargeJpaEntity fromDomain(Charge charge) {
        ChargeJpaEntity e = new ChargeJpaEntity();
        e.id = charge.getId();
        e.groupId = charge.getGroupId();
        e.memberId = charge.getMemberId();
        e.guestId = charge.getGuestId();
        e.type = charge.getType();
        e.amount = charge.getAmount();
        e.referenceMatchId = charge.getReferenceMatchId();
        e.referenceMonth = charge.getReferenceMonth();
        e.status = charge.getStatus();
        e.createdAt = charge.getCreatedAt();
        e.updatedAt = charge.getUpdatedAt();
        return e;
    }
}
