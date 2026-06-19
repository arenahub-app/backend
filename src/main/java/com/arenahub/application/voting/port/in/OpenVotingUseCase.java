package com.arenahub.application.voting.port.in;

import com.arenahub.presentation.voting.dto.VotingResponse;

import java.time.Instant;
import java.util.UUID;

public interface OpenVotingUseCase {
    VotingResponse execute(Command command);

    record Command(UUID groupId, UUID userId, Instant deadline) {}
}
