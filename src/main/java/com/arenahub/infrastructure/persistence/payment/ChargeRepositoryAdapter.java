package com.arenahub.infrastructure.persistence.payment;

import com.arenahub.application.payment.port.out.ChargeRepository;
import com.arenahub.domain.payment.Charge;
import com.arenahub.domain.payment.PaymentAttempt;
import com.arenahub.domain.payment.vo.ChargeStatus;
import com.arenahub.infrastructure.persistence.group.GroupMemberJpaRepository;
import com.arenahub.infrastructure.persistence.match.MatchGuestJpaRepository;
import com.arenahub.infrastructure.persistence.match.MatchJpaRepository;
import com.arenahub.infrastructure.persistence.user.UserJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ChargeRepositoryAdapter implements ChargeRepository {

    private final ChargeJpaRepository chargeRepo;
    private final PaymentAttemptJpaRepository attemptRepo;
    private final GroupMemberJpaRepository memberRepo;
    private final UserJpaRepository userRepo;
    private final MatchJpaRepository matchRepo;
    private final MatchGuestJpaRepository guestRepo;

    public ChargeRepositoryAdapter(ChargeJpaRepository chargeRepo,
                                    PaymentAttemptJpaRepository attemptRepo,
                                    GroupMemberJpaRepository memberRepo,
                                    UserJpaRepository userRepo,
                                    MatchJpaRepository matchRepo,
                                    MatchGuestJpaRepository guestRepo) {
        this.chargeRepo = chargeRepo;
        this.attemptRepo = attemptRepo;
        this.memberRepo = memberRepo;
        this.userRepo = userRepo;
        this.matchRepo = matchRepo;
        this.guestRepo = guestRepo;
    }

    @Override
    public Charge save(Charge charge) {
        return chargeRepo.save(ChargeJpaEntity.fromDomain(charge)).toDomain();
    }

    @Override
    public Optional<Charge> findByIdAndGroupId(UUID chargeId, UUID groupId) {
        return chargeRepo.findByIdAndGroupId(chargeId, groupId).map(ChargeJpaEntity::toDomain);
    }

    @Override
    public boolean existsByMatchIdAndMemberIdAndStatusIn(UUID matchId, UUID memberId,
                                                          List<ChargeStatus> statuses) {
        return chargeRepo.existsByReferenceMatchIdAndMemberIdAndStatusIn(matchId, memberId, statuses);
    }

    @Override
    public Charge createDaily(UUID groupId, UUID memberId, BigDecimal amount, UUID matchId) {
        Charge charge = Charge.createDaily(groupId, memberId, amount, matchId);
        return chargeRepo.save(ChargeJpaEntity.fromDomain(charge)).toDomain();
    }

    @Override
    public List<ChargeView> findByGroupId(UUID groupId, ChargeStatus status, UUID matchId,
                                           int page, int size) {
        return chargeRepo.findByGroupIdFiltered(groupId, status, matchId, PageRequest.of(page, size))
                .stream().map(this::toView).toList();
    }

    @Override
    public long countByGroupId(UUID groupId, ChargeStatus status, UUID matchId) {
        return chargeRepo.countByGroupIdFiltered(groupId, status, matchId);
    }

    @Override
    public List<ChargeView> findByMatchId(UUID matchId) {
        return chargeRepo.findByReferenceMatchId(matchId).stream()
                .map(this::toView).toList();
    }

    @Override
    public List<ChargeView> findByMemberId(UUID memberId) {
        return chargeRepo.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(this::toView).toList();
    }

    @Override
    public PaymentAttempt saveAttempt(PaymentAttempt attempt) {
        return attemptRepo.save(PaymentAttemptJpaEntity.fromDomain(attempt)).toDomain();
    }

    @Override
    public Optional<PaymentAttempt> findAttemptById(UUID attemptId) {
        return attemptRepo.findById(attemptId).map(PaymentAttemptJpaEntity::toDomain);
    }

    @Override
    public List<PaymentAttempt> findAttemptsByChargeId(UUID chargeId) {
        return attemptRepo.findByChargeIdOrderBySubmittedAtDesc(chargeId).stream()
                .map(PaymentAttemptJpaEntity::toDomain).toList();
    }

    @Override
    public boolean existsPendingAttemptByChargeId(UUID chargeId) {
        return attemptRepo.existsByChargeIdAndValidationResultIsNull(chargeId);
    }

    private ChargeView toView(ChargeJpaEntity e) {
        String memberName = null;
        if (e.getMemberId() != null) {
            memberName = memberRepo.findById(e.getMemberId())
                    .flatMap(m -> userRepo.findById(m.getUserId()))
                    .map(u -> u.getName())
                    .orElse(null);
        }

        String guestName = null;
        if (e.getGuestId() != null) {
            guestName = guestRepo.findById(e.getGuestId())
                    .map(g -> g.getName())
                    .orElse(null);
        }

        var latestResult = attemptRepo.findByChargeIdOrderBySubmittedAtDesc(e.getId())
                .stream().findFirst()
                .map(a -> a.getValidationResult())
                .orElse(null);

        var matchAt = e.getReferenceMatchId() != null
                ? matchRepo.findById(e.getReferenceMatchId())
                        .map(m -> m.getScheduledAt()).orElse(null)
                : null;

        return new ChargeView(e.getId(), e.getMemberId(), memberName,
                e.getGuestId(), guestName,
                e.getType(), e.getAmount(), e.getReferenceMatchId(), matchAt,
                e.getStatus(), latestResult, e.getCreatedAt());
    }
}
