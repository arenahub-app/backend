package com.arenahub.infrastructure.persistence.match;

import com.arenahub.domain.match.vo.PresenceListStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchJpaRepository extends JpaRepository<MatchJpaEntity, UUID> {

    Optional<MatchJpaEntity> findByIdAndGroupId(UUID id, UUID groupId);

    List<MatchJpaEntity> findByGroupIdAndScheduledAtAfterOrderByScheduledAtAsc(UUID groupId, Instant cutoff);

    List<MatchJpaEntity> findByGroupIdAndScheduledAtBeforeOrderByScheduledAtDesc(UUID groupId, Instant cutoff);

    List<MatchJpaEntity> findByGroupIdOrderByScheduledAtDesc(UUID groupId);

    List<MatchJpaEntity> findByPresenceListStatusAndListClosesAtBefore(PresenceListStatus status, Instant cutoff);
}
