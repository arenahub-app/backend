CREATE TABLE group_invites (
    id          UUID                     NOT NULL DEFAULT gen_random_uuid(),
    group_id    UUID                     NOT NULL,
    created_by  UUID                     NOT NULL,
    token       VARCHAR(36)              NOT NULL,
    usage_count INTEGER                  NOT NULL DEFAULT 0,
    max_usages  INTEGER                  NOT NULL DEFAULT 50,
    active      BOOLEAN                  NOT NULL DEFAULT TRUE,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_group_invites PRIMARY KEY (id),

    CONSTRAINT fk_group_invites_group
        FOREIGN KEY (group_id) REFERENCES groups (id),

    CONSTRAINT fk_group_invites_creator
        FOREIGN KEY (created_by) REFERENCES users (id),

    CONSTRAINT chk_group_invites_usage_count
        CHECK (usage_count >= 0 AND usage_count <= max_usages),

    CONSTRAINT chk_group_invites_max_usages
        CHECK (max_usages > 0)
);

CREATE UNIQUE INDEX uq_group_invites_token ON group_invites (token);
CREATE INDEX idx_group_invites_group_active ON group_invites (group_id, active);

COMMENT ON TABLE group_invites IS 'Links de convite de grupo — expiram em 7 dias ou 50 usos (RN-GRP-004) — BC: Group Management';
