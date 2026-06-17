package com.arenahub.application.payment.port.out;

import com.arenahub.domain.financial.FinancialEntry;

public interface FinancialEntryRepository {

    FinancialEntry save(FinancialEntry entry);
}
