package com.arenahub.application.group.port.in;

import com.arenahub.presentation.group.dto.InviteResponse;

import java.util.UUID;

public interface GenerateInviteUseCase {

    record Command(UUID groupId, UUID userId) {}

    InviteResponse execute(Command command);
}
