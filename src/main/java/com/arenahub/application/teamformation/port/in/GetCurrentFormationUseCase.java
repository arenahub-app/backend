package com.arenahub.application.teamformation.port.in;

import com.arenahub.presentation.teamformation.dto.TeamFormationResponse;

import java.util.UUID;

public interface GetCurrentFormationUseCase {

    TeamFormationResponse execute(Command command);

    record Command(UUID groupId, UUID matchId, UUID actorUserId) {}
}
