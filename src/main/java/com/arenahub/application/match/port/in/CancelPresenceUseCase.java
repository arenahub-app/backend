package com.arenahub.application.match.port.in;

import java.util.UUID;

public interface CancelPresenceUseCase {

    void execute(Command command);

    record Command(UUID groupId, UUID matchId, UUID actorUserId) {}
}
