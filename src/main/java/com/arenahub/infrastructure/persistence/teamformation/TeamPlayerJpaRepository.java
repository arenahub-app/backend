package com.arenahub.infrastructure.persistence.teamformation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeamPlayerJpaRepository extends JpaRepository<TeamPlayerJpaEntity, UUID> {

    List<TeamPlayerJpaEntity> findByTeamId(UUID teamId);

    void deleteByTeamIdIn(List<UUID> teamIds);
}
