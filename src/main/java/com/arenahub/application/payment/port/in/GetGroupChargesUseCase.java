package com.arenahub.application.payment.port.in;

import com.arenahub.domain.payment.vo.ChargeStatus;
import com.arenahub.presentation.payment.dto.ChargePageResponse;

import java.util.UUID;

public interface GetGroupChargesUseCase {

    record Command(UUID groupId, UUID actorUserId,
                   ChargeStatus status, UUID matchId,
                   int page, int size) {}

    ChargePageResponse execute(Command command);
}
