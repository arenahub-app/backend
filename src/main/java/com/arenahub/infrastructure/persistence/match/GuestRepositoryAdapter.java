package com.arenahub.infrastructure.persistence.match;

import com.arenahub.application.match.port.out.GuestRepository;
import com.arenahub.domain.match.MatchGuest;
import com.arenahub.domain.match.vo.GuestStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GuestRepositoryAdapter implements GuestRepository {

    private final MatchGuestJpaRepository guestRepo;

    public GuestRepositoryAdapter(MatchGuestJpaRepository guestRepo) {
        this.guestRepo = guestRepo;
    }

    @Override
    public MatchGuest save(MatchGuest guest) {
        return guestRepo.save(MatchGuestJpaEntity.fromDomain(guest)).toDomain();
    }

    @Override
    public Optional<MatchGuest> findById(UUID id) {
        return guestRepo.findById(id).map(MatchGuestJpaEntity::toDomain);
    }

    @Override
    public Optional<MatchGuest> findByIdAndMatchId(UUID id, UUID matchId) {
        return guestRepo.findByIdAndMatchId(id, matchId).map(MatchGuestJpaEntity::toDomain);
    }

    @Override
    public List<MatchGuest> findByMatchId(UUID matchId) {
        return guestRepo.findByMatchId(matchId).stream()
                .map(MatchGuestJpaEntity::toDomain).toList();
    }

    @Override
    public List<MatchGuest> findConfirmedByMatchId(UUID matchId) {
        return guestRepo.findByMatchIdAndStatus(matchId, GuestStatus.CONFIRMED).stream()
                .map(MatchGuestJpaEntity::toDomain).toList();
    }

    @Override
    public long countOccupiedByMatchId(UUID matchId) {
        return guestRepo.countByMatchIdAndStatusIn(matchId,
                List.of(GuestStatus.CONFIRMED, GuestStatus.PAYMENT_PENDING));
    }

    @Override
    public void delete(UUID id) {
        guestRepo.deleteById(id);
    }
}
