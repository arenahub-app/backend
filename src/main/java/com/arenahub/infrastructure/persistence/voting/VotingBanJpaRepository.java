package com.arenahub.infrastructure.persistence.voting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VotingBanJpaRepository extends JpaRepository<VotingBanJpaEntity, UUID> {

    Optional<VotingBanJpaEntity> findByVotingIdAndMemberId(UUID votingId, UUID memberId);

    List<VotingBanJpaEntity> findByVotingId(UUID votingId);
}
