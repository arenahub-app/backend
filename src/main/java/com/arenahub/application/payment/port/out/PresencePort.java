package com.arenahub.application.payment.port.out;

import java.util.UUID;

public interface PresencePort {

    void confirmAfterPayment(UUID matchId, UUID memberId);
}
