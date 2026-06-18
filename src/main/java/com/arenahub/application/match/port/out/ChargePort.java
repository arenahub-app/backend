package com.arenahub.application.match.port.out;

import com.arenahub.domain.payment.vo.ChargeStatus;

import java.math.BigDecimal;
import java.util.UUID;

public interface ChargePort {

    boolean existsPendingOrApproved(UUID matchId, UUID memberId);

    ChargeView createDaily(UUID groupId, UUID memberId, BigDecimal amount, UUID matchId);

    ChargeView createDailyForGuest(UUID groupId, UUID guestId, BigDecimal amount, UUID matchId);

    boolean existsPendingOrApprovedForGuest(UUID matchId, UUID guestId);

    void cancelGuestCharge(UUID matchId, UUID guestId, String note);

    record ChargeView(UUID chargeId, BigDecimal amount, String pixKey, ChargeStatus status) {}
}
