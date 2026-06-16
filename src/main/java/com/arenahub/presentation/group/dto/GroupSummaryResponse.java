package com.arenahub.presentation.group.dto;

import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.group.vo.Sport;

import java.util.UUID;

public record GroupSummaryResponse(
        UUID id,
        String name,
        Sport sport,
        String photoUrl,
        GroupStatus status,
        int memberCount,
        GroupRole myRole
) {}
