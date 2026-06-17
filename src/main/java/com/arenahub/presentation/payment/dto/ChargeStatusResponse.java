package com.arenahub.presentation.payment.dto;

import com.arenahub.domain.payment.vo.ChargeStatus;

import java.time.Instant;
import java.util.UUID;

public record ChargeStatusResponse(
        UUID chargeId,
        ChargeStatus status,
        Instant updatedAt
) {}
