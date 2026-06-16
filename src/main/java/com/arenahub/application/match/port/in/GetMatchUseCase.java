package com.arenahub.application.match.port.in;

import com.arenahub.presentation.match.dto.MatchResponse;

import java.util.UUID;

public interface GetMatchUseCase {

    MatchResponse execute(Command command);

    record Command(UUID groupId, UUID matchId, UUID actorUserId) {}
}
