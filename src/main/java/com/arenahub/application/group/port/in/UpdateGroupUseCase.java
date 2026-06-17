package com.arenahub.application.group.port.in;

import com.arenahub.domain.group.vo.Sport;
import com.arenahub.presentation.group.dto.GroupResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateGroupUseCase {

    record Command(UUID groupId, UUID userId, String name, Sport sport,
                   String description, String pixKey, BigDecimal matchFee) {}

    GroupResponse execute(Command command);
}
