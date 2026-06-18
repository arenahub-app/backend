package com.arenahub.application.payment.port.out;

import com.arenahub.domain.payment.Charge;
import com.arenahub.domain.payment.PaymentAttempt;
import com.arenahub.domain.payment.vo.ChargeStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChargeRepository {

    Charge save(Charge charge);

    Optional<Charge> findByIdAndGroupId(UUID chargeId, UUID groupId);

    boolean existsByMatchIdAndMemberIdAndStatusIn(UUID matchId, UUID memberId,
                                                   List<ChargeStatus> statuses);

    Charge createDaily(UUID groupId, UUID memberId, BigDecimal amount, UUID matchId);

    List<ChargeView> findByGroupId(UUID groupId, ChargeStatus status, UUID matchId,
                                    int page, int size);

    long countByGroupId(UUID groupId, ChargeStatus status, UUID matchId);

    List<ChargeView> findByMatchId(UUID matchId);

    List<ChargeView> findByMemberId(UUID memberId);

    PaymentAttempt saveAttempt(PaymentAttempt attempt);

    Optional<PaymentAttempt> findAttemptById(UUID attemptId);

    List<PaymentAttempt> findAttemptsByChargeId(UUID chargeId);

    boolean existsPendingAttemptByChargeId(UUID chargeId);

    record ChargeView(
            UUID chargeId,
            UUID memberId,
            String memberName,
            UUID guestId,
            String guestName,
            com.arenahub.domain.payment.vo.ChargeType type,
            java.math.BigDecimal amount,
            UUID referenceMatchId,
            java.time.Instant matchScheduledAt,
            com.arenahub.domain.payment.vo.ChargeStatus status,
            com.arenahub.domain.payment.vo.ValidationResult latestAttemptStatus,
            java.time.Instant createdAt
    ) {}
}
