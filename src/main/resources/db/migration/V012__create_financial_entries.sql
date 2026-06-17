CREATE TABLE financial_entries (
    id               UUID                     NOT NULL DEFAULT gen_random_uuid(),
    group_id         UUID                     NOT NULL,
    type             VARCHAR(10)              NOT NULL,
    category         VARCHAR(15)              NOT NULL,
    amount           NUMERIC(10, 2)           NOT NULL,
    description      TEXT                     NOT NULL,
    match_id         UUID,
    reference_month  VARCHAR(7),
    source_charge_id UUID,
    registered_by    UUID                     NOT NULL,
    registered_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status           VARCHAR(10)              NOT NULL DEFAULT 'ACTIVE',
    reversal_reason  TEXT,
    reversed_by      UUID,
    reversed_at      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_financial_entries PRIMARY KEY (id),

    CONSTRAINT fk_financial_entries_group
        FOREIGN KEY (group_id) REFERENCES groups (id),

    CONSTRAINT fk_financial_entries_match
        FOREIGN KEY (match_id) REFERENCES matches (id),

    CONSTRAINT fk_financial_entries_charge
        FOREIGN KEY (source_charge_id) REFERENCES charges (id),

    CONSTRAINT fk_financial_entries_registrant
        FOREIGN KEY (registered_by) REFERENCES users (id),

    CONSTRAINT fk_financial_entries_reverser
        FOREIGN KEY (reversed_by) REFERENCES users (id),

    CONSTRAINT chk_financial_entries_type
        CHECK (type IN ('REVENUE', 'EXPENSE')),

    CONSTRAINT chk_financial_entries_category
        CHECK (category IN ('DAILY', 'SUBSCRIPTION', 'REFEREE', 'COURT', 'OTHER')),

    CONSTRAINT chk_financial_entries_amount
        CHECK (amount > 0),

    CONSTRAINT chk_financial_entries_status
        CHECK (status IN ('ACTIVE', 'REVERSED')),

    CONSTRAINT chk_financial_entries_reversal
        CHECK (status != 'REVERSED' OR (reversal_reason IS NOT NULL AND reversed_by IS NOT NULL AND reversed_at IS NOT NULL)),

    CONSTRAINT chk_financial_entries_reference_month_format
        CHECK (reference_month IS NULL OR reference_month ~ '^\d{4}-(0[1-9]|1[0-2])$'),

    CONSTRAINT chk_financial_entries_revenue_category
        CHECK (type != 'REVENUE' OR category IN ('DAILY', 'SUBSCRIPTION')),

    CONSTRAINT chk_financial_entries_expense_category
        CHECK (type != 'EXPENSE' OR category IN ('REFEREE', 'COURT', 'OTHER'))
);

CREATE INDEX idx_financial_entries_group       ON financial_entries (group_id, registered_at DESC);
CREATE INDEX idx_financial_entries_month       ON financial_entries (group_id, reference_month)
    WHERE reference_month IS NOT NULL;
CREATE INDEX idx_financial_entries_match       ON financial_entries (match_id)
    WHERE match_id IS NOT NULL;
CREATE INDEX idx_financial_entries_type_status ON financial_entries (group_id, type, status);
CREATE INDEX idx_financial_entries_charge      ON financial_entries (source_charge_id)
    WHERE source_charge_id IS NOT NULL;

COMMENT ON TABLE financial_entries IS 'Registros contábeis imutáveis de receitas e despesas — BC: Financial';
COMMENT ON COLUMN financial_entries.status IS 'Nunca excluído — estornado com REVERSED + motivo obrigatório (RN-FIN-005)';
COMMENT ON COLUMN financial_entries.source_charge_id IS 'Preenchido para receitas originadas de cobranças aprovadas — rastreabilidade Payments → Financial';
