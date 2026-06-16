package com.arenahub.presentation.match.dto;

import com.arenahub.domain.group.vo.PlayerPosition;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WaitingEntryResponse(
        UUID id,
        UUID memberId,
        String userName,
        String role,
        BigDecimal skill,
        PlayerPosition position,
        int queuePosition,
        Instant createdAt
) {}
