package com.arenahub.application.payment.port.in;

import com.arenahub.presentation.payment.dto.PaymentAttemptResponse;

import java.util.UUID;

public interface SubmitReceiptUseCase {

    record Command(UUID groupId, UUID chargeId, UUID actorUserId,
                   byte[] fileContent, String originalFilename, String contentType) {}

    PaymentAttemptResponse execute(Command command);
}
