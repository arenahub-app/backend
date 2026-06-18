package com.arenahub.application.match.port.in;

import java.util.UUID;

public interface RemoveGuestUseCase {

    void execute(Command cmd);

    record Command(UUID groupId, UUID matchId, UUID guestId, UUID actorUserId) {}
}
