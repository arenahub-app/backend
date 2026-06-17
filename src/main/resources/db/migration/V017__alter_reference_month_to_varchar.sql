ALTER TABLE charges
    ALTER COLUMN reference_month TYPE VARCHAR(7);

ALTER TABLE financial_entries
    ALTER COLUMN reference_month TYPE VARCHAR(7);
