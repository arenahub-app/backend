package com.arenahub.presentation.payment.dto;

import com.arenahub.domain.payment.vo.ChargeStatus;
import com.arenahub.domain.payment.vo.ChargeType;
import com.arenahub.domain.payment.vo.ValidationResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ChargeResponse(
        UUID chargeId,
        UUID memberId,
        String memberName,
        UUID guestId,
        String guestName,
        ChargeType type,
        BigDecimal amount,
        UUID referenceMatchId,
        Instant matchScheduledAt,
        ChargeStatus status,
        ValidationResult latestAttemptStatus,
        Instant createdAt
) {}
