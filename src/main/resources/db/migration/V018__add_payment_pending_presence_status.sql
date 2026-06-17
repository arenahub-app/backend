ALTER TABLE presence_entries
    DROP CONSTRAINT chk_presence_entries_status;

ALTER TABLE presence_entries
    ADD CONSTRAINT chk_presence_entries_status
        CHECK (status IN ('CONFIRMED', 'DECLINED', 'BANNED_PENDING', 'PAYMENT_PENDING'));
