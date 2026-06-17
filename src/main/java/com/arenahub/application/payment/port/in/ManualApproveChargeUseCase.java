package com.arenahub.application.payment.port.in;

import com.arenahub.presentation.payment.dto.ChargeStatusResponse;

import java.util.UUID;

public interface ManualApproveChargeUseCase {

    record Command(UUID groupId, UUID chargeId, UUID actorUserId, String note) {}

    ChargeStatusResponse execute(Command command);
}
