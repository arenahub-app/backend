ALTER TABLE presences
    DROP CONSTRAINT chk_presences_status;

ALTER TABLE presences
    ADD CONSTRAINT chk_presences_status
        CHECK (status IN ('CONFIRMED', 'DECLINED', 'BANNED_PENDING', 'PAYMENT_PENDING'));
