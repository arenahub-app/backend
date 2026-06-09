CREATE TABLE group_members (
    id                  UUID                     NOT NULL DEFAULT gen_random_uuid(),
    group_id            UUID                     NOT NULL,
    user_id             UUID                     NOT NULL,
    role                VARCHAR(10)              NOT NULL,
    skill               NUMERIC(3, 1)            NOT NULL DEFAULT 3.0,
    skill_source        VARCHAR(10)              NOT NULL DEFAULT 'DEFAULT',
    position            VARCHAR(20),
    is_subscriber       BOOLEAN                  NOT NULL DEFAULT FALSE,
    subscription_amount NUMERIC(10, 2),
    presence_banned     BOOLEAN                  NOT NULL DEFAULT FALSE,
    presence_ban_reason TEXT,
    joined_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_group_members PRIMARY KEY (id),

    CONSTRAINT fk_group_members_group
        FOREIGN KEY (group_id) REFERENCES groups (id),

    CONSTRAINT fk_group_members_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT uq_group_members_group_user
        UNIQUE (group_id, user_id),

    CONSTRAINT chk_group_members_role
        CHECK (role IN ('OWNER', 'ADMIN', 'PLAYER', 'REFEREE')),

    CONSTRAINT chk_group_members_skill_range
        CHECK (skill >= 1.0 AND skill <= 6.0),

    CONSTRAINT chk_group_members_skill_source
        CHECK (skill_source IN ('DEFAULT', 'VOTING', 'MANUAL')),

    CONSTRAINT chk_group_members_position CHECK (
        position IS NULL OR position IN (
            'GOALKEEPER', 'DEFENDER', 'LATERAL', 'MIDFIELDER', 'FORWARD',
            'SETTER', 'WING_SPIKER', 'MIDDLE_BLOCKER', 'OPPOSITE', 'LIBERO',
            'POINT_GUARD', 'SHOOTING_GUARD', 'SMALL_FORWARD', 'POWER_FORWARD', 'CENTER',
            'ATTACKER_FV', 'DEFENDER_FV',
            'RIGHT_BT', 'LEFT_BT',
            'OTHER'
        )
    ),

    CONSTRAINT chk_group_members_subscription
        CHECK (NOT is_subscriber OR subscription_amount IS NOT NULL),

    CONSTRAINT chk_group_members_subscription_amount
        CHECK (subscription_amount IS NULL OR subscription_amount > 0),

    CONSTRAINT chk_group_members_ban_reason
        CHECK (NOT presence_banned OR presence_ban_reason IS NOT NULL)
);

CREATE INDEX idx_group_members_group_id    ON group_members (group_id);
CREATE INDEX idx_group_members_user_id     ON group_members (user_id);
CREATE INDEX idx_group_members_group_role  ON group_members (group_id, role);

COMMENT ON TABLE group_members IS 'Membros de um grupo com papel e skill — entidade dentro do agregado Group — BC: Group Management';
COMMENT ON COLUMN group_members.skill IS 'Nível de habilidade 1.0–6.0; padrão 3.0; atualizado por votação ou manualmente';
COMMENT ON COLUMN group_members.presence_banned IS 'Quando TRUE, jogador precisa justificar para confirmar presença (RN-PRE-004)';
