package com.arenahub.application.payment.port.in;

import com.arenahub.presentation.payment.dto.ChargeStatusResponse;

import java.util.UUID;

public interface ApproveAttemptUseCase {

    record Command(UUID groupId, UUID chargeId, UUID attemptId,
                   UUID actorUserId, String reviewNote) {}

    ChargeStatusResponse execute(Command command);
}
