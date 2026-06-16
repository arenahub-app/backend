package com.arenahub.infrastructure.persistence.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PresenceBanHistoryJpaRepository extends JpaRepository<PresenceBanHistoryJpaEntity, UUID> {
}
