CREATE TABLE team_formations (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid(),
    match_id        UUID                     NOT NULL,
    group_id        UUID                     NOT NULL,
    number_of_teams INTEGER                  NOT NULL,
    formation_type  VARCHAR(20)              NOT NULL,
    confirmed_by    UUID                     NOT NULL,
    confirmed_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_team_formations PRIMARY KEY (id),

    CONSTRAINT fk_team_formations_match
        FOREIGN KEY (match_id) REFERENCES matches (id),

    CONSTRAINT fk_team_formations_confirmer
        FOREIGN KEY (confirmed_by) REFERENCES users (id),

    CONSTRAINT chk_team_formations_number_of_teams
        CHECK (number_of_teams >= 2),

    CONSTRAINT chk_team_formations_formation_type
        CHECK (formation_type IN ('AUTOMATIC', 'MANUAL_ADJUSTED'))
);

CREATE INDEX idx_team_formations_match   ON team_formations (match_id);
CREATE INDEX idx_team_formations_group   ON team_formations (group_id, confirmed_at DESC);

COMMENT ON TABLE team_formations IS 'Formação de times de uma partida — BC: Team Formation';


CREATE TABLE teams (
    id            UUID          NOT NULL DEFAULT gen_random_uuid(),
    formation_id  UUID          NOT NULL,
    group_id      UUID          NOT NULL,
    name          VARCHAR(50)   NOT NULL,
    average_skill NUMERIC(3, 1) NOT NULL,
    player_count  INTEGER       NOT NULL DEFAULT 0,

    CONSTRAINT pk_teams PRIMARY KEY (id),

    CONSTRAINT fk_teams_formation
        FOREIGN KEY (formation_id) REFERENCES team_formations (id),

    CONSTRAINT chk_teams_average_skill
        CHECK (average_skill >= 1.0 AND average_skill <= 6.0),

    CONSTRAINT chk_teams_player_count
        CHECK (player_count >= 0)
);

CREATE INDEX idx_teams_formation ON teams (formation_id);

COMMENT ON TABLE teams IS 'Times de uma formação — BC: Team Formation';


CREATE TABLE team_players (
    id        UUID        NOT NULL DEFAULT gen_random_uuid(),
    team_id   UUID        NOT NULL,
    group_id  UUID        NOT NULL,
    member_id UUID        NOT NULL,
    position  VARCHAR(20),

    CONSTRAINT pk_team_players PRIMARY KEY (id),

    CONSTRAINT fk_team_players_team
        FOREIGN KEY (team_id) REFERENCES teams (id),

    CONSTRAINT fk_team_players_member
        FOREIGN KEY (member_id) REFERENCES group_members (id),

    CONSTRAINT uq_team_players_team_member
        UNIQUE (team_id, member_id)
);

CREATE INDEX idx_team_players_team   ON team_players (team_id);
CREATE INDEX idx_team_players_group  ON team_players (group_id);

COMMENT ON TABLE team_players IS 'Jogadores de um time — árbitros excluídos (RN-TIM-002) — BC: Team Formation';
