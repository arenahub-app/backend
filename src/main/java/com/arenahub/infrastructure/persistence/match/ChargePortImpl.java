package com.arenahub.infrastructure.persistence.match;

import com.arenahub.application.match.port.out.ChargePort;
import com.arenahub.domain.payment.Charge;
import com.arenahub.domain.payment.vo.ChargeStatus;
import com.arenahub.infrastructure.persistence.group.GroupJpaRepository;
import com.arenahub.infrastructure.persistence.payment.ChargeJpaEntity;
import com.arenahub.infrastructure.persistence.payment.ChargeJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class ChargePortImpl implements ChargePort {

    private final ChargeJpaRepository chargeRepo;
    private final GroupJpaRepository groupRepo;

    public ChargePortImpl(ChargeJpaRepository chargeRepo, GroupJpaRepository groupRepo) {
        this.chargeRepo = chargeRepo;
        this.groupRepo = groupRepo;
    }

    @Override
    public boolean existsPendingOrApproved(UUID matchId, UUID memberId) {
        return chargeRepo.existsByReferenceMatchIdAndMemberIdAndStatusIn(
                matchId, memberId, List.of(ChargeStatus.PENDING, ChargeStatus.APPROVED));
    }

    @Override
    public ChargeView createDaily(UUID groupId, UUID memberId, BigDecimal amount, UUID matchId) {
        Charge charge = Charge.createDaily(groupId, memberId, amount, matchId);
        ChargeJpaEntity saved = chargeRepo.save(ChargeJpaEntity.fromDomain(charge));

        String pixKey = groupRepo.findByIdAndDeletedAtIsNull(groupId)
                .map(g -> g.getPixKey()).orElse(null);

        return new ChargeView(saved.getId(), saved.getAmount(), pixKey, saved.getStatus());
    }
}
