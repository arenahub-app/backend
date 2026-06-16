package com.arenahub.application.match.port.in;

import com.arenahub.presentation.match.dto.MatchSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ListMatchesUseCase {

    List<MatchSummaryResponse> execute(Command command);

    record Command(UUID groupId, UUID actorUserId, String filter) {}
}
