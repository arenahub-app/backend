package com.arenahub.infrastructure.persistence.financial;

import com.arenahub.application.payment.port.out.FinancialEntryRepository;
import com.arenahub.domain.financial.FinancialEntry;
import org.springframework.stereotype.Component;

@Component
public class FinancialEntryRepositoryAdapter implements FinancialEntryRepository {

    private final FinancialEntryJpaRepository repo;

    public FinancialEntryRepositoryAdapter(FinancialEntryJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public FinancialEntry save(FinancialEntry entry) {
        repo.save(FinancialEntryJpaEntity.fromDomain(entry));
        return entry;
    }
}
