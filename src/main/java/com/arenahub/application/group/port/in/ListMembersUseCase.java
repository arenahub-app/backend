package com.arenahub.application.group.port.in;

import com.arenahub.presentation.group.dto.MemberResponse;

import java.util.List;
import java.util.UUID;

public interface ListMembersUseCase {

    record Command(UUID groupId, UUID userId) {}

    List<MemberResponse> execute(Command command);
}
