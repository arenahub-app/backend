package com.arenahub.application.voting.port.in;

import com.arenahub.presentation.voting.dto.VotingResultsResponse;

import java.util.UUID;

public interface GetVotingResultsUseCase {
    VotingResultsResponse execute(Command command);

    record Command(UUID groupId, UUID votingId, UUID userId) {}
}
