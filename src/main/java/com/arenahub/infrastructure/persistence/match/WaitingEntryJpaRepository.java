package com.arenahub.infrastructure.persistence.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaitingEntryJpaRepository extends JpaRepository<WaitingEntryJpaEntity, UUID> {

    Optional<WaitingEntryJpaEntity> findByMatchIdAndMemberId(UUID matchId, UUID memberId);

    Optional<WaitingEntryJpaEntity> findFirstByMatchIdOrderByPositionAsc(UUID matchId);

    List<WaitingEntryJpaEntity> findByMatchIdOrderByPositionAsc(UUID matchId);

    long countByMatchId(UUID matchId);
}
