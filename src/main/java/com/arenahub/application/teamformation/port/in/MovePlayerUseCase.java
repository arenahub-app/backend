package com.arenahub.application.teamformation.port.in;

import com.arenahub.presentation.teamformation.dto.TeamFormationResponse;

import java.util.UUID;

public interface MovePlayerUseCase {

    TeamFormationResponse execute(Command command);

    record Command(UUID groupId, UUID matchId, UUID formationId, UUID actorUserId,
                   UUID memberId, UUID toTeamId) {}
}
