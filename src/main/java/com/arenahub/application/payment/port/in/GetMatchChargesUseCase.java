package com.arenahub.application.payment.port.in;

import com.arenahub.presentation.payment.dto.ChargeResponse;

import java.util.List;
import java.util.UUID;

public interface GetMatchChargesUseCase {

    record Command(UUID groupId, UUID matchId, UUID actorUserId) {}

    List<ChargeResponse> execute(Command command);
}
