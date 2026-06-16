package com.arenahub.application.match.port.in;

import com.arenahub.presentation.match.dto.MatchResponse;

import java.time.Instant;
import java.util.UUID;

public interface UpdateMatchUseCase {

    MatchResponse execute(Command command);

    record Command(UUID groupId, UUID matchId, UUID actorUserId,
                   Instant scheduledAt, String locationName, String locationAddress,
                   Integer maxPlayers) {}
}
