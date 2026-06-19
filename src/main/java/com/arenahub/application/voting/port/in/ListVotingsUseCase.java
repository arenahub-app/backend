package com.arenahub.application.voting.port.in;

import com.arenahub.presentation.voting.dto.VotingSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ListVotingsUseCase {
    List<VotingSummaryResponse> execute(Command command);

    record Command(UUID groupId, UUID userId) {}
}
