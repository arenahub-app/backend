package com.arenahub.application.match.port.in;

import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.presentation.match.dto.GuestResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface AddGuestUseCase {

    GuestResponse execute(Command cmd);

    record Command(UUID groupId, UUID matchId, UUID actorUserId,
                   String name, BigDecimal skill, PlayerPosition position) {}
}
