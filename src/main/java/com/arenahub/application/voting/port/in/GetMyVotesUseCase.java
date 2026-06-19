package com.arenahub.application.voting.port.in;

import com.arenahub.presentation.voting.dto.MyVotesResponse;

import java.util.UUID;

public interface GetMyVotesUseCase {
    MyVotesResponse execute(Command command);

    record Command(UUID groupId, UUID votingId, UUID userId) {}
}
