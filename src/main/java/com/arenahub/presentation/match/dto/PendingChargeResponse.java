package com.arenahub.presentation.match.dto;

import com.arenahub.domain.payment.vo.ChargeStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PendingChargeResponse(
        UUID chargeId,
        BigDecimal amount,
        String pixKey,
        ChargeStatus status
) {}
