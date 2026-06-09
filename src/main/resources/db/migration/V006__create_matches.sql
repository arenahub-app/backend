CREATE TABLE matches (
    id                   UUID                     NOT NULL DEFAULT gen_random_uuid(),
    group_id             UUID                     NOT NULL,
    scheduled_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    list_closes_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    location_name        VARCHAR(200)             NOT NULL,
    location_address     TEXT,
    max_players          INTEGER                  NOT NULL,
    status               VARCHAR(15)              NOT NULL DEFAULT 'SCHEDULED',
    presence_list_status VARCHAR(10)              NOT NULL DEFAULT 'OPEN',
    created_by           UUID                     NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_matches PRIMARY KEY (id),

    CONSTRAINT fk_matches_group
        FOREIGN KEY (group_id) REFERENCES groups (id),

    CONSTRAINT fk_matches_creator
        FOREIGN KEY (created_by) REFERENCES users (id),

    CONSTRAINT chk_matches_status
        CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED')),

    CONSTRAINT chk_matches_presence_list_status
        CHECK (presence_list_status IN ('OPEN', 'CLOSED')),

    CONSTRAINT chk_matches_max_players
        CHECK (max_players > 0),

    CONSTRAINT chk_matches_list_closes_before_match
        CHECK (list_closes_at < scheduled_at),

    CONSTRAINT chk_matches_scheduled_future
        CHECK (scheduled_at > created_at)
);

CREATE INDEX idx_matches_group_id        ON matches (group_id);
CREATE INDEX idx_matches_scheduled_at    ON matches (group_id, scheduled_at DESC);
CREATE INDEX idx_matches_list_closes_at  ON matches (list_closes_at)
    WHERE presence_list_status = 'OPEN';

COMMENT ON TABLE matches IS 'Partidas de um grupo — Aggregate Root do BC Match & Presence';
COMMENT ON COLUMN matches.list_closes_at IS 'scheduled_at - 1h; calculado pela aplicação; indexado para o scheduler de fechamento automático';
