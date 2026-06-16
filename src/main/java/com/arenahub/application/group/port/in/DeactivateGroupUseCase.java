package com.arenahub.application.group.port.in;

import java.util.UUID;

public interface DeactivateGroupUseCase {

    record Command(UUID groupId, UUID userId) {}

    void execute(Command command);
}
