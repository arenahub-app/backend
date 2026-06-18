CREATE TABLE match_guests (
    id           UUID                     NOT NULL DEFAULT gen_random_uuid(),
    match_id     UUID                     NOT NULL,
    group_id     UUID                     NOT NULL,
    name         VARCHAR(100)             NOT NULL,
    skill        NUMERIC(3,1)             NOT NULL,
    position     VARCHAR(30)              NOT NULL,
    status       VARCHAR(20)              NOT NULL DEFAULT 'CONFIRMED',
    added_by     UUID                     NOT NULL,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_match_guests PRIMARY KEY (id),
    CONSTRAINT fk_match_guests_match FOREIGN KEY (match_id) REFERENCES matches (id),
    CONSTRAINT fk_match_guests_group FOREIGN KEY (group_id) REFERENCES groups (id),
    CONSTRAINT fk_match_guests_added_by FOREIGN KEY (added_by) REFERENCES users (id),
    CONSTRAINT chk_match_guests_status CHECK (status IN ('CONFIRMED', 'PAYMENT_PENDING')),
    CONSTRAINT chk_match_guests_skill CHECK (skill >= 1.0 AND skill <= 5.0),
    CONSTRAINT chk_match_guests_confirmed_at CHECK (status != 'CONFIRMED' OR confirmed_at IS NOT NULL)
);

CREATE INDEX idx_match_guests_match ON match_guests (match_id, group_id);
CREATE INDEX idx_match_guests_status ON match_guests (match_id, status);

ALTER TABLE charges ALTER COLUMN member_id DROP NOT NULL;
ALTER TABLE charges ADD COLUMN guest_id UUID REFERENCES match_guests (id);
ALTER TABLE charges ADD CONSTRAINT chk_charges_subject
    CHECK (
        (member_id IS NOT NULL AND guest_id IS NULL) OR
        (member_id IS NULL     AND guest_id IS NOT NULL)
    );

CREATE INDEX idx_charges_guest ON charges (guest_id) WHERE guest_id IS NOT NULL;
