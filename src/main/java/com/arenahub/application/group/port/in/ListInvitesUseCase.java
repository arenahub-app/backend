package com.arenahub.application.group.port.in;

import com.arenahub.presentation.group.dto.InviteResponse;

import java.util.List;
import java.util.UUID;

public interface ListInvitesUseCase {

    record Command(UUID groupId, UUID userId) {}

    List<InviteResponse> execute(Command command);
}
