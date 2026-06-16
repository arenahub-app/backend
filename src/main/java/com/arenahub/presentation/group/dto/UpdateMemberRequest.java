package com.arenahub.presentation.group.dto;

import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.PlayerPosition;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record UpdateMemberRequest(
        GroupRole role,
        @DecimalMin("1.0") @DecimalMax("6.0") Double skill,
        PlayerPosition position,
        Boolean subscriptionActive
) {}
