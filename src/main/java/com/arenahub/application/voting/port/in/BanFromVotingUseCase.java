package com.arenahub.application.voting.port.in;

import java.util.UUID;

public interface BanFromVotingUseCase {
    void execute(Command command);

    record Command(UUID groupId, UUID votingId, UUID memberId, String reason, UUID userId) {}
}
