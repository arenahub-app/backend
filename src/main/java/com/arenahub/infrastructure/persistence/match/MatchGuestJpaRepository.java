package com.arenahub.infrastructure.persistence.match;

import com.arenahub.domain.match.vo.GuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchGuestJpaRepository extends JpaRepository<MatchGuestJpaEntity, UUID> {

    List<MatchGuestJpaEntity> findByMatchId(UUID matchId);

    List<MatchGuestJpaEntity> findByMatchIdAndStatus(UUID matchId, GuestStatus status);

    long countByMatchIdAndStatus(UUID matchId, GuestStatus status);

    long countByMatchIdAndStatusIn(UUID matchId, List<GuestStatus> statuses);

    Optional<MatchGuestJpaEntity> findByIdAndMatchId(UUID id, UUID matchId);
}
