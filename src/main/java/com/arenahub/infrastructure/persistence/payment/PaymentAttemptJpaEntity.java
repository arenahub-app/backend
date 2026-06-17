package com.arenahub.infrastructure.persistence.payment;

import com.arenahub.domain.payment.PaymentAttempt;
import com.arenahub.domain.payment.vo.ValidationResult;
import com.arenahub.domain.payment.vo.ValidationSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts")
@Getter
@Setter
@NoArgsConstructor
public class PaymentAttemptJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "charge_id", nullable = false, columnDefinition = "uuid")
    private UUID chargeId;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "file_key", length = 500)
    private String fileKey;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_result", length = 15)
    private ValidationResult validationResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_source", length = 10)
    private ValidationSource validationSource;

    @Column(name = "validation_reason", columnDefinition = "TEXT")
    private String validationReason;

    @Column(name = "reviewed_by", columnDefinition = "uuid")
    private UUID reviewedBy;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    public PaymentAttempt toDomain() {
        return PaymentAttempt.reconstitute(id, chargeId, groupId, fileKey, contentType,
                validationResult, validationSource, validationReason,
                reviewedBy, reviewNote, submittedAt, validatedAt, reviewedAt);
    }

    public static PaymentAttemptJpaEntity fromDomain(PaymentAttempt a) {
        PaymentAttemptJpaEntity e = new PaymentAttemptJpaEntity();
        e.id = a.getId();
        e.chargeId = a.getChargeId();
        e.groupId = a.getGroupId();
        e.fileKey = a.getFileKey();
        e.contentType = a.getContentType();
        e.validationResult = a.getValidationResult();
        e.validationSource = a.getValidationSource();
        e.validationReason = a.getValidationReason();
        e.reviewedBy = a.getReviewedBy();
        e.reviewNote = a.getReviewNote();
        e.submittedAt = a.getSubmittedAt();
        e.validatedAt = a.getValidatedAt();
        e.reviewedAt = a.getReviewedAt();
        return e;
    }
}
