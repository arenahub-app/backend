package com.arenahub.application.match.port.in;

import com.arenahub.presentation.match.dto.PresenceEntryResponse;

import java.util.UUID;

public interface AdminForcePresenceUseCase {

    PresenceEntryResponse execute(Command command);

    record Command(UUID groupId, UUID matchId, UUID memberId, UUID actorUserId) {}
}
