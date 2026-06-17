package com.arenahub.application.match.port.out;

import com.arenahub.domain.payment.vo.ChargeStatus;

import java.math.BigDecimal;
import java.util.UUID;

public interface ChargePort {

    boolean existsPendingOrApproved(UUID matchId, UUID memberId);

    ChargeView createDaily(UUID groupId, UUID memberId, BigDecimal amount, UUID matchId);

    record ChargeView(UUID chargeId, BigDecimal amount, String pixKey, ChargeStatus status) {}
}
