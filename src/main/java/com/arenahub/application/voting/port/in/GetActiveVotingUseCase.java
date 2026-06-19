package com.arenahub.application.voting.port.in;

import com.arenahub.presentation.voting.dto.VotingResponse;

import java.util.UUID;

public interface GetActiveVotingUseCase {
    VotingResponse execute(Command command);

    record Command(UUID groupId, UUID userId) {}
}
