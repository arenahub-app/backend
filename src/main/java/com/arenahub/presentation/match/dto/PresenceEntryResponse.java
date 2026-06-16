package com.arenahub.presentation.match.dto;

import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.domain.match.vo.PresenceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PresenceEntryResponse(
        UUID id,
        UUID memberId,
        String userName,
        String role,
        BigDecimal skill,
        PlayerPosition position,
        PresenceStatus status,
        Instant confirmedAt
) {}
