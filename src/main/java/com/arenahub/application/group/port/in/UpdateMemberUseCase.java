package com.arenahub.application.group.port.in;

import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.presentation.group.dto.MemberResponse;

import java.util.UUID;

public interface UpdateMemberUseCase {

    record Command(UUID groupId, UUID memberId, UUID requesterId,
                   GroupRole role, Double skill, PlayerPosition position,
                   Boolean subscriptionActive) {}

    MemberResponse execute(Command command);
}
