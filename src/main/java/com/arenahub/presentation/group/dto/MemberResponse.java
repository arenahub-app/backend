package com.arenahub.presentation.group.dto;

import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.domain.group.vo.SkillSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MemberResponse(
        UUID id,
        UUID userId,
        UUID groupId,
        String userName,
        GroupRole role,
        BigDecimal skill,
        SkillSource skillSource,
        PlayerPosition position,
        boolean isSubscriber,
        Instant joinedAt
) {}
