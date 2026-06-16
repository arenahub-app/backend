package com.arenahub.application.group.port.in;

import java.util.UUID;

public interface DeactivateInviteUseCase {

    record Command(UUID groupId, UUID inviteId, UUID userId) {}

    void execute(Command command);
}
