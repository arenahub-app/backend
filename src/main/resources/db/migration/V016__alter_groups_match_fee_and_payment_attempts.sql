ALTER TABLE groups
    ADD COLUMN match_fee NUMERIC(10, 2);

ALTER TABLE groups
    ADD CONSTRAINT chk_groups_match_fee
        CHECK (match_fee IS NULL OR match_fee > 0);

COMMENT ON COLUMN groups.match_fee IS
    'Valor cobrado por partida (diária). NULL = grupo sem cobrança obrigatória.';

ALTER TABLE payment_attempts
    ALTER COLUMN file_key DROP NOT NULL;

ALTER TABLE payment_attempts
    ADD CONSTRAINT chk_payment_attempts_file_required
        CHECK (validation_source = 'MANUAL' OR file_key IS NOT NULL);

COMMENT ON COLUMN payment_attempts.file_key IS
    'Chave do arquivo no R2. NULL apenas quando validation_source = MANUAL (aprovação sem comprovante).';

CREATE TRIGGER trg_updated_at_charges
    BEFORE UPDATE ON charges
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
