CREATE TABLE referee_profiles (
    id          UUID                     NOT NULL DEFAULT gen_random_uuid(),
    group_id    UUID                     NOT NULL,
    member_id   UUID                     NOT NULL,
    charge_type VARCHAR(15)              NOT NULL,
    amount      NUMERIC(10, 2)           NOT NULL,
    active      BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_referee_profiles PRIMARY KEY (id),

    CONSTRAINT fk_referee_profiles_group
        FOREIGN KEY (group_id) REFERENCES groups (id),

    CONSTRAINT fk_referee_profiles_member
        FOREIGN KEY (member_id) REFERENCES group_members (id),

    CONSTRAINT chk_referee_profiles_charge_type
        CHECK (charge_type IN ('PER_MATCH', 'MONTHLY')),

    CONSTRAINT chk_referee_profiles_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_referee_profiles_group ON referee_profiles (group_id, active);
CREATE UNIQUE INDEX uq_referee_profiles_member ON referee_profiles (member_id) WHERE active = TRUE;

COMMENT ON TABLE referee_profiles IS 'Perfil de árbitro com cobrança configurada — BC: Group Management';
COMMENT ON COLUMN referee_profiles.charge_type IS 'PER_MATCH: cobrança por partida; MONTHLY: cobrança mensal';
