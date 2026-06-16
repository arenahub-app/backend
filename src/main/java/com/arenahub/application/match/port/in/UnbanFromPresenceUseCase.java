package com.arenahub.application.match.port.in;

import java.util.UUID;

public interface UnbanFromPresenceUseCase {

    void execute(Command command);

    record Command(UUID groupId, UUID memberId, UUID actorUserId) {}
}
