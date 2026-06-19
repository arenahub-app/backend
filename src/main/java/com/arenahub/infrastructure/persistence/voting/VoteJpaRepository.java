package com.arenahub.infrastructure.persistence.voting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, UUID> {

    Optional<VoteJpaEntity> findByVotingIdAndVoterIdAndTargetMemberId(UUID votingId, UUID voterId, UUID targetMemberId);

    List<VoteJpaEntity> findByVotingId(UUID votingId);

    List<VoteJpaEntity> findByVotingIdAndVoterId(UUID votingId, UUID voterId);

    long countByVotingIdAndVoterId(UUID votingId, UUID voterId);
}
