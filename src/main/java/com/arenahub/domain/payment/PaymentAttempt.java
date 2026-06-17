package com.arenahub.domain.payment;

import com.arenahub.domain.payment.vo.ValidationResult;
import com.arenahub.domain.payment.vo.ValidationSource;

import java.time.Instant;
import java.util.UUID;

public class PaymentAttempt {

    private UUID id;
    private UUID chargeId;
    private UUID groupId;
    private String fileKey;
    private String contentType;
    private ValidationResult validationResult;
    private ValidationSource validationSource;
    private String validationReason;
    private UUID reviewedBy;
    private String reviewNote;
    private Instant submittedAt;
    private Instant validatedAt;
    private Instant reviewedAt;

    private PaymentAttempt() {}

    public static PaymentAttempt createWithFile(UUID chargeId, UUID groupId,
                                                 String fileKey, String contentType) {
        PaymentAttempt a = new PaymentAttempt();
        a.id = UUID.randomUUID();
        a.chargeId = chargeId;
        a.groupId = groupId;
        a.fileKey = fileKey;
        a.contentType = contentType;
        a.submittedAt = Instant.now();
        return a;
    }

    public static PaymentAttempt createManual(UUID chargeId, UUID groupId,
                                               UUID reviewedBy, String reviewNote) {
        PaymentAttempt a = new PaymentAttempt();
        a.id = UUID.randomUUID();
        a.chargeId = chargeId;
        a.groupId = groupId;
        a.validationResult = ValidationResult.APPROVED;
        a.validationSource = ValidationSource.MANUAL;
        a.reviewedBy = reviewedBy;
        a.reviewNote = reviewNote;
        a.submittedAt = Instant.now();
        a.reviewedAt = Instant.now();
        return a;
    }

    public static PaymentAttempt reconstitute(UUID id, UUID chargeId, UUID groupId,
                                               String fileKey, String contentType,
                                               ValidationResult validationResult,
                                               ValidationSource validationSource,
                                               String validationReason,
                                               UUID reviewedBy, String reviewNote,
                                               Instant submittedAt, Instant validatedAt,
                                               Instant reviewedAt) {
        PaymentAttempt a = new PaymentAttempt();
        a.id = id;
        a.chargeId = chargeId;
        a.groupId = groupId;
        a.fileKey = fileKey;
        a.contentType = contentType;
        a.validationResult = validationResult;
        a.validationSource = validationSource;
        a.validationReason = validationReason;
        a.reviewedBy = reviewedBy;
        a.reviewNote = reviewNote;
        a.submittedAt = submittedAt;
        a.validatedAt = validatedAt;
        a.reviewedAt = reviewedAt;
        return a;
    }

    public void approve(UUID adminId, String note) {
        this.validationResult = ValidationResult.APPROVED;
        this.validationSource = ValidationSource.MANUAL;
        this.reviewedBy = adminId;
        this.reviewNote = note;
        this.reviewedAt = Instant.now();
    }

    public void reject(UUID adminId, String note) {
        this.validationResult = ValidationResult.REJECTED;
        this.validationSource = ValidationSource.MANUAL;
        this.reviewedBy = adminId;
        this.reviewNote = note;
        this.reviewedAt = Instant.now();
    }

    public boolean isPendingReview() {
        return validationResult == null;
    }

    public UUID getId() { return id; }
    public UUID getChargeId() { return chargeId; }
    public UUID getGroupId() { return groupId; }
    public String getFileKey() { return fileKey; }
    public String getContentType() { return contentType; }
    public ValidationResult getValidationResult() { return validationResult; }
    public ValidationSource getValidationSource() { return validationSource; }
    public String getValidationReason() { return validationReason; }
    public UUID getReviewedBy() { return reviewedBy; }
    public String getReviewNote() { return reviewNote; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getValidatedAt() { return validatedAt; }
    public Instant getReviewedAt() { return reviewedAt; }
}
