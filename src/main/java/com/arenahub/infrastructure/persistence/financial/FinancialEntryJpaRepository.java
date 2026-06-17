package com.arenahub.infrastructure.persistence.financial;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FinancialEntryJpaRepository extends JpaRepository<FinancialEntryJpaEntity, UUID> {
}
