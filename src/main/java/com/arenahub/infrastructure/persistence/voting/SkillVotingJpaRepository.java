package com.arenahub.infrastructure.persistence.voting;

import com.arenahub.domain.voting.vo.VotingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillVotingJpaRepository extends JpaRepository<SkillVotingJpaEntity, UUID> {

    Optional<SkillVotingJpaEntity> findByIdAndGroupId(UUID id, UUID groupId);

    Optional<SkillVotingJpaEntity> findByGroupIdAndStatus(UUID groupId, VotingStatus status);

    List<SkillVotingJpaEntity> findByGroupIdOrderByOpenedAtDesc(UUID groupId);

    List<SkillVotingJpaEntity> findByStatusAndDeadlineBefore(VotingStatus status, Instant cutoff);
}
