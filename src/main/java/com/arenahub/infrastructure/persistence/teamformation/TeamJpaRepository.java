package com.arenahub.infrastructure.persistence.teamformation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeamJpaRepository extends JpaRepository<TeamJpaEntity, UUID> {

    List<TeamJpaEntity> findByFormationId(UUID formationId);

    void deleteByFormationId(UUID formationId);
}
