package com.arenahub.application.voting.port.in;

import com.arenahub.presentation.voting.dto.VoteResponse;

import java.util.UUID;

public interface CastVoteUseCase {
    VoteResponse execute(Command command);

    record Command(UUID groupId, UUID votingId, UUID targetMemberId, UUID userId, int stars) {}
}
