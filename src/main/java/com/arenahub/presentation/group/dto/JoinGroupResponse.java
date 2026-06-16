package com.arenahub.presentation.group.dto;

import com.arenahub.domain.group.vo.GroupRole;

import java.util.UUID;

public record JoinGroupResponse(
        UUID groupId,
        UUID memberId,
        GroupRole role
) {}
