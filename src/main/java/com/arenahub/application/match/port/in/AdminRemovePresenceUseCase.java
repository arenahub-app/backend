package com.arenahub.application.match.port.in;

import java.util.UUID;

public interface AdminRemovePresenceUseCase {

    void execute(Command command);

    record Command(UUID groupId, UUID matchId, UUID memberId, UUID actorUserId) {}
}
