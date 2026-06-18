package com.arenahub.presentation.match.dto;

import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.domain.match.vo.GuestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GuestResponse(
        UUID id,
        UUID matchId,
        String name,
        BigDecimal skill,
        PlayerPosition position,
        GuestStatus status,
        UUID chargeId,
        Instant confirmedAt,
        Instant createdAt
) {}
