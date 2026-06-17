package com.arenahub.infrastructure.persistence.match;

import com.arenahub.application.payment.port.out.PresencePort;
import com.arenahub.domain.match.vo.PresenceStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PresencePortImpl implements PresencePort {

    private final PresenceEntryJpaRepository presenceRepo;

    public PresencePortImpl(PresenceEntryJpaRepository presenceRepo) {
        this.presenceRepo = presenceRepo;
    }

    @Override
    public void confirmAfterPayment(UUID matchId, UUID memberId) {
        presenceRepo.findByMatchIdAndMemberId(matchId, memberId).ifPresent(entity -> {
            if (entity.getStatus() == PresenceStatus.PAYMENT_PENDING) {
                var entry = entity.toDomain();
                entry.confirmAfterPayment();
                presenceRepo.save(PresenceEntryJpaEntity.fromDomain(entry));
            }
        });
    }
}
