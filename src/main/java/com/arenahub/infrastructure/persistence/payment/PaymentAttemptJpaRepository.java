package com.arenahub.infrastructure.persistence.payment;

import com.arenahub.domain.payment.vo.ValidationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentAttemptJpaRepository extends JpaRepository<PaymentAttemptJpaEntity, UUID> {

    List<PaymentAttemptJpaEntity> findByChargeIdOrderBySubmittedAtDesc(UUID chargeId);

    boolean existsByChargeIdAndValidationResultIsNull(UUID chargeId);
}
