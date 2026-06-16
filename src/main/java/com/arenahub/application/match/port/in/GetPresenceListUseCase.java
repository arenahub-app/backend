package com.arenahub.application.match.port.in;

import com.arenahub.presentation.match.dto.PresenceListResponse;

import java.util.UUID;

public interface GetPresenceListUseCase {

    PresenceListResponse execute(Command command);

    record Command(UUID groupId, UUID matchId, UUID actorUserId) {}
}
