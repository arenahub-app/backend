package com.arenahub.application.group.port.in;

import java.util.UUID;

public interface RemoveMemberUseCase {

    record Command(UUID groupId, UUID memberId, UUID requesterId) {}

    void execute(Command command);
}
