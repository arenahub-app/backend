package com.arenahub.presentation.group.dto;

import com.arenahub.domain.group.vo.Sport;

import java.time.Instant;
import java.util.UUID;

public record InvitePreviewResponse(
        UUID groupId,
        String groupName,
        Sport sport,
        String photoUrl,
        int memberCount,
        Instant expiresAt
) {}
