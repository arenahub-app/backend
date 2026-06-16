package com.arenahub.domain.group;

import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.group.vo.Sport;

import java.util.UUID;

public record GroupSummary(
        UUID id,
        String name,
        Sport sport,
        String photoUrl,
        GroupStatus status,
        int memberCount,
        GroupRole myRole
) {}
