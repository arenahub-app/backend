package com.arenahub.presentation.payment.dto;

import com.arenahub.domain.payment.vo.ChargeStatus;
import com.arenahub.domain.payment.vo.ChargeType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChargeDetailResponse(
        UUID chargeId,
        UUID memberId,
        String memberName,
        ChargeType type,
        BigDecimal amount,
        String pixKey,
        UUID referenceMatchId,
        Instant matchScheduledAt,
        ChargeStatus status,
        Instant createdAt,
        List<PaymentAttemptResponse> attempts
) {}
