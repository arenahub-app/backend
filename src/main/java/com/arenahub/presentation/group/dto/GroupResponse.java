package com.arenahub.presentation.group.dto;

import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.group.vo.Sport;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GroupResponse(
        UUID id,
        String name,
        Sport sport,
        String description,
        String photoUrl,
        String pixKey,
        BigDecimal matchFee,
        GroupStatus status,
        int memberCount,
        GroupRole myRole,
        Instant createdAt,
        Instant updatedAt
) {}
