package com.arenahub.application.group.port.in;

import com.arenahub.presentation.group.dto.GroupResponse;

import java.util.UUID;

public interface GetGroupUseCase {

    record Command(UUID groupId, UUID userId) {}

    GroupResponse execute(Command command);
}
