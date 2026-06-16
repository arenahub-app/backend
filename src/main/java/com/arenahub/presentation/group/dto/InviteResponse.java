package com.arenahub.presentation.group.dto;

import java.time.Instant;
import java.util.UUID;

public record InviteResponse(
        UUID id,
        UUID groupId,
        String token,
        int usageCount,
        int maxUsages,
        boolean active,
        Instant expiresAt,
        Instant createdAt
) {}
