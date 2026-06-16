package com.arenahub.application.match.port.in;

import com.arenahub.presentation.match.dto.PresenceActionResponse;

import java.util.UUID;

public interface ConfirmPresenceUseCase {

    PresenceActionResponse execute(Command command);

    record Command(UUID groupId, UUID matchId, UUID actorUserId) {}
}
