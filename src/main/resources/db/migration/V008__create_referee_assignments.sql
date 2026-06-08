CREATE TABLE referee_assignments (
    id                  UUID                     NOT NULL DEFAULT gen_random_uuid(),
    match_id            UUID                     NOT NULL,
    group_id            UUID                     NOT NULL,
    referee_profile_id  UUID                     NOT NULL,
    financial_entry_id  UUID,
    assigned_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_referee_assignments PRIMARY KEY (id),

    CONSTRAINT fk_referee_assignments_match
        FOREIGN KEY (match_id) REFERENCES matches (id),

    CONSTRAINT fk_referee_assignments_referee_profile
        FOREIGN KEY (referee_profile_id) REFERENCES referee_profiles (id),

    CONSTRAINT uq_referee_assignments_match
        UNIQUE (match_id)
);

CREATE INDEX idx_referee_assignments_group ON referee_assignments (group_id);

COMMENT ON TABLE referee_assignments IS 'Associação de árbitro a partida — um árbitro por partida (RN-ARB-006) — BC: Match & Presence';
COMMENT ON COLUMN referee_assignments.financial_entry_id IS 'Referência à despesa gerada automaticamente no BC Financial (RN-ARB-004); preenchido após criação da FinancialEntry';
