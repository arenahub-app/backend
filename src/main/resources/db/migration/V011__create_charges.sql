CREATE TABLE charges (
    id                  UUID                     NOT NULL DEFAULT gen_random_uuid(),
    group_id            UUID                     NOT NULL,
    member_id           UUID                     NOT NULL,
    type                VARCHAR(15)              NOT NULL,
    amount              NUMERIC(10, 2)           NOT NULL,
    reference_match_id  UUID,
    reference_month     CHAR(7),
    status              VARCHAR(15)              NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_charges PRIMARY KEY (id),

    CONSTRAINT fk_charges_group
        FOREIGN KEY (group_id) REFERENCES groups (id),

    CONSTRAINT fk_charges_member
        FOREIGN KEY (member_id) REFERENCES group_members (id),

    CONSTRAINT fk_charges_match
        FOREIGN KEY (reference_match_id) REFERENCES matches (id),

    CONSTRAINT chk_charges_type
        CHECK (type IN ('DAILY', 'SUBSCRIPTION')),

    CONSTRAINT chk_charges_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),

    CONSTRAINT chk_charges_amount
        CHECK (amount > 0),

    CONSTRAINT chk_charges_daily_has_match
        CHECK (type != 'DAILY' OR reference_match_id IS NOT NULL),

    CONSTRAINT chk_charges_subscription_has_month
        CHECK (type != 'SUBSCRIPTION' OR reference_month IS NOT NULL),

    CONSTRAINT chk_charges_reference_month_format
        CHECK (reference_month IS NULL OR reference_month ~ '^\d{4}-(0[1-9]|1[0-2])$')
);

CREATE INDEX idx_charges_group_member  ON charges (group_id, member_id);
CREATE INDEX idx_charges_group_status  ON charges (group_id, status);
CREATE INDEX idx_charges_match         ON charges (reference_match_id) WHERE reference_match_id IS NOT NULL;
CREATE INDEX idx_charges_month         ON charges (group_id, reference_month) WHERE reference_month IS NOT NULL;

COMMENT ON TABLE charges IS 'Cobranças de diária ou mensalidade — Aggregate Root do BC Payments';
COMMENT ON COLUMN charges.reference_month IS 'Formato YYYY-MM; obrigatório para cobranças SUBSCRIPTION (RN-PAG-004)';


CREATE TABLE payment_attempts (
    id                UUID                     NOT NULL DEFAULT gen_random_uuid(),
    charge_id         UUID                     NOT NULL,
    group_id          UUID                     NOT NULL,
    file_key          VARCHAR(500)             NOT NULL,
    content_type      VARCHAR(100)             NOT NULL,
    validation_result VARCHAR(15),
    validation_source VARCHAR(10),
    validation_reason TEXT,
    reviewed_by       UUID,
    review_note       TEXT,
    submitted_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    validated_at      TIMESTAMP WITH TIME ZONE,
    reviewed_at       TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_payment_attempts PRIMARY KEY (id),

    CONSTRAINT fk_payment_attempts_charge
        FOREIGN KEY (charge_id) REFERENCES charges (id),

    CONSTRAINT fk_payment_attempts_reviewer
        FOREIGN KEY (reviewed_by) REFERENCES users (id),

    CONSTRAINT chk_payment_attempts_content_type
        CHECK (content_type IN ('image/jpeg', 'image/png', 'application/pdf')),

    CONSTRAINT chk_payment_attempts_validation_result
        CHECK (validation_result IS NULL
            OR validation_result IN ('APPROVED', 'REJECTED', 'MANUAL_REVIEW')),

    CONSTRAINT chk_payment_attempts_validation_source
        CHECK (validation_source IS NULL
            OR validation_source IN ('AI', 'MANUAL')),

    CONSTRAINT chk_payment_attempts_manual_has_reviewer
        CHECK (validation_source != 'MANUAL' OR reviewed_by IS NOT NULL)
);

CREATE INDEX idx_payment_attempts_charge ON payment_attempts (charge_id);
CREATE INDEX idx_payment_attempts_group  ON payment_attempts (group_id, submitted_at DESC);

COMMENT ON TABLE payment_attempts IS 'Tentativas de pagamento com comprovante — BC: Payments';
COMMENT ON COLUMN payment_attempts.file_key IS 'Chave do arquivo no Cloudflare R2 — nunca a URL direta';
COMMENT ON COLUMN payment_attempts.content_type IS 'Apenas JPEG, PNG e PDF aceitos (RN-PAG-007)';
