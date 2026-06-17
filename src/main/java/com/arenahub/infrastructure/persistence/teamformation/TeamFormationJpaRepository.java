package com.arenahub.infrastructure.persistence.teamformation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeamFormationJpaRepository extends JpaRepository<TeamFormationJpaEntity, UUID> {

    Optional<TeamFormationJpaEntity> findByMatchIdAndGroupId(UUID matchId, UUID groupId);

    Optional<TeamFormationJpaEntity> findByIdAndGroupId(UUID id, UUID groupId);
}
