package com.arenahub.application.group.port.in;

import com.arenahub.presentation.group.dto.JoinGroupResponse;

import java.util.UUID;

public interface JoinByInviteUseCase {

    record Command(String token, UUID userId) {}

    JoinGroupResponse execute(Command command);
}
