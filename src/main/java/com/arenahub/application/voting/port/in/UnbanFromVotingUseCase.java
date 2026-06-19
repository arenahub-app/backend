package com.arenahub.application.voting.port.in;

import java.util.UUID;

public interface UnbanFromVotingUseCase {
    void execute(Command command);

    record Command(UUID groupId, UUID votingId, UUID memberId, UUID userId) {}
}
