package com.arenahub.application.group.port.in;

import com.arenahub.domain.group.vo.Sport;
import com.arenahub.presentation.group.dto.GroupResponse;

import java.util.UUID;

public interface CreateGroupUseCase {

    record Command(UUID userId, String name, Sport sport, String description) {}

    GroupResponse execute(Command command);
}
