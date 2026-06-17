package com.arenahub.presentation.payment.dto;

import com.arenahub.domain.payment.vo.ValidationResult;
import com.arenahub.domain.payment.vo.ValidationSource;

import java.time.Instant;
import java.util.UUID;

public record PaymentAttemptResponse(
        UUID attemptId,
        UUID chargeId,
        String fileUrl,
        String contentType,
        ValidationResult validationResult,
        ValidationSource validationSource,
        String reviewNote,
        Instant submittedAt,
        Instant reviewedAt
) {}
