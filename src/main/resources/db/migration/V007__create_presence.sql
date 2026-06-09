CREATE TABLE presence_entries (
    id                   UUID                     NOT NULL DEFAULT gen_random_uuid(),
    match_id             UUID                     NOT NULL,
    group_id             UUID                     NOT NULL,
    member_id            UUID                     NOT NULL,
    status               VARCHAR(15)              NOT NULL,
    justification        TEXT,
    justification_status VARCHAR(10),
    confirmed_at         TIMESTAMP WITH TIME ZONE,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_presence_entries PRIMARY KEY (id),

    CONSTRAINT fk_presence_entries_match
        FOREIGN KEY (match_id) REFERENCES matches (id),

    CONSTRAINT fk_presence_entries_member
        FOREIGN KEY (member_id) REFERENCES group_members (id),

    CONSTRAINT uq_presence_entries_match_member
        UNIQUE (match_id, member_id),

    CONSTRAINT chk_presence_entries_status
        CHECK (status IN ('CONFIRMED', 'DECLINED', 'BANNED_PENDING')),

    CONSTRAINT chk_presence_entries_justification_status
        CHECK (justification_status IS NULL
            OR justification_status IN ('PENDING', 'APPROVED', 'REJECTED')),

    CONSTRAINT chk_presence_entries_banned_has_justification
        CHECK (status != 'BANNED_PENDING' OR justification IS NOT NULL),

    CONSTRAINT chk_presence_entries_confirmed_at
        CHECK (status != 'CONFIRMED' OR confirmed_at IS NOT NULL)
);

CREATE INDEX idx_presence_entries_match    ON presence_entries (match_id, group_id);
CREATE INDEX idx_presence_entries_member   ON presence_entries (member_id, group_id);
CREATE INDEX idx_presence_entries_status   ON presence_entries (match_id, status);

COMMENT ON TABLE presence_entries IS 'Confirmações de presença por partida — BC: Match & Presence';


CREATE TABLE waiting_entries (
    id                    UUID                     NOT NULL DEFAULT gen_random_uuid(),
    match_id              UUID                     NOT NULL,
    group_id              UUID                     NOT NULL,
    member_id             UUID                     NOT NULL,
    position              INTEGER                  NOT NULL,
    notified_at           TIMESTAMP WITH TIME ZONE,
    notification_deadline TIMESTAMP WITH TIME ZONE,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_waiting_entries PRIMARY KEY (id),

    CONSTRAINT fk_waiting_entries_match
        FOREIGN KEY (match_id) REFERENCES matches (id),

    CONSTRAINT fk_waiting_entries_member
        FOREIGN KEY (member_id) REFERENCES group_members (id),

    CONSTRAINT uq_waiting_entries_match_member
        UNIQUE (match_id, member_id),

    CONSTRAINT chk_waiting_entries_position
        CHECK (position > 0),

    CONSTRAINT chk_waiting_entries_notification
        CHECK (notified_at IS NULL OR notification_deadline IS NOT NULL)
);

CREATE INDEX idx_waiting_entries_match_position ON waiting_entries (match_id, position);
CREATE INDEX idx_waiting_entries_group          ON waiting_entries (group_id);

COMMENT ON TABLE waiting_entries IS 'Fila de espera de partida — FIFO por position — BC: Match & Presence';
COMMENT ON COLUMN waiting_entries.notification_deadline IS 'notified_at + 30min; jogador tem 30 min para confirmar após ser notificado (RN-PRE-003)';


CREATE TABLE presence_ban_history (
    id           UUID                     NOT NULL DEFAULT gen_random_uuid(),
    group_id     UUID                     NOT NULL,
    member_id    UUID                     NOT NULL,
    action       VARCHAR(10)              NOT NULL,
    reason       TEXT,
    performed_by UUID                     NOT NULL,
    performed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_presence_ban_history PRIMARY KEY (id),

    CONSTRAINT fk_presence_ban_history_member
        FOREIGN KEY (member_id) REFERENCES group_members (id),

    CONSTRAINT fk_presence_ban_history_performer
        FOREIGN KEY (performed_by) REFERENCES users (id),

    CONSTRAINT chk_presence_ban_history_action
        CHECK (action IN ('BANNED', 'UNBANNED')),

    CONSTRAINT chk_presence_ban_history_ban_reason
        CHECK (action != 'BANNED' OR reason IS NOT NULL)
);

CREATE INDEX idx_presence_ban_history_member ON presence_ban_history (member_id, group_id);
CREATE INDEX idx_presence_ban_history_group  ON presence_ban_history (group_id, performed_at DESC);

COMMENT ON TABLE presence_ban_history IS 'Log imutável de banimentos de presença — auditoria de conformidade — BC: Match & Presence';
