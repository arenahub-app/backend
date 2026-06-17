package com.arenahub.application.payment.port.in;

import com.arenahub.presentation.payment.dto.ChargeDetailResponse;

import java.util.UUID;

public interface GetChargeDetailUseCase {

    record Command(UUID groupId, UUID chargeId, UUID actorUserId) {}

    ChargeDetailResponse execute(Command command);
}
