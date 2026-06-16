package com.arenahub.application.match.port.in;

import com.arenahub.presentation.group.dto.MemberResponse;

import java.util.UUID;

public interface BanFromPresenceUseCase {

    MemberResponse execute(Command command);

    record Command(UUID groupId, UUID memberId, UUID actorUserId, String reason) {}
}
