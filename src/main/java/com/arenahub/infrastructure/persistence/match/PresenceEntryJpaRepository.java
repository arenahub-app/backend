package com.arenahub.infrastructure.persistence.match;

import com.arenahub.domain.match.vo.PresenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PresenceEntryJpaRepository extends JpaRepository<PresenceEntryJpaEntity, UUID> {

    Optional<PresenceEntryJpaEntity> findByMatchIdAndMemberId(UUID matchId, UUID memberId);

    List<PresenceEntryJpaEntity> findByMatchId(UUID matchId);

    List<PresenceEntryJpaEntity> findByMatchIdAndStatus(UUID matchId, PresenceStatus status);

    long countByMatchIdAndStatus(UUID matchId, PresenceStatus status);
}
