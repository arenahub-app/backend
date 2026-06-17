package com.arenahub.application.teamformation.port.in;

import java.util.UUID;

public interface DeleteFormationUseCase {

    void execute(Command command);

    record Command(UUID groupId, UUID matchId, UUID formationId, UUID actorUserId) {}
}
