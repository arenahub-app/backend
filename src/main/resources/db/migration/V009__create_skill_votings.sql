CREATE TABLE skill_votings (
    id         UUID                     NOT NULL DEFAULT gen_random_uuid(),
    group_id   UUID                     NOT NULL,
    status     VARCHAR(10)              NOT NULL DEFAULT 'OPEN',
    opened_by  UUID                     NOT NULL,
    opened_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deadline   TIMESTAMP WITH TIME ZONE NOT NULL,
    closed_at  TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_skill_votings PRIMARY KEY (id),

    CONSTRAINT fk_skill_votings_group
        FOREIGN KEY (group_id) REFERENCES groups (id),

    CONSTRAINT fk_skill_votings_opener
        FOREIGN KEY (opened_by) REFERENCES users (id),

    CONSTRAINT chk_skill_votings_status
        CHECK (status IN ('OPEN', 'CLOSED')),

    CONSTRAINT chk_skill_votings_deadline_after_open
        CHECK (deadline > opened_at),

    CONSTRAINT chk_skill_votings_closed_at
        CHECK (status != 'CLOSED' OR closed_at IS NOT NULL)
);

CREATE UNIQUE INDEX uq_skill_votings_one_active_per_group
    ON skill_votings (group_id)
    WHERE status = 'OPEN';

CREATE INDEX idx_skill_votings_group_status ON skill_votings (group_id, status);

COMMENT ON TABLE skill_votings IS 'Votações de habilidade — apenas uma ativa por grupo (RN-VOT-001) — BC: Skill Voting';


CREATE TABLE votes (
    id               UUID                     NOT NULL DEFAULT gen_random_uuid(),
    voting_id        UUID                     NOT NULL,
    group_id         UUID                     NOT NULL,
    voter_id         UUID                     NOT NULL,
    target_member_id UUID                     NOT NULL,
    stars            INTEGER                  NOT NULL,
    voted_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_votes PRIMARY KEY (id),

    CONSTRAINT fk_votes_voting
        FOREIGN KEY (voting_id) REFERENCES skill_votings (id),

    CONSTRAINT fk_votes_voter
        FOREIGN KEY (voter_id) REFERENCES group_members (id),

    CONSTRAINT fk_votes_target
        FOREIGN KEY (target_member_id) REFERENCES group_members (id),

    CONSTRAINT uq_votes_voter_target
        UNIQUE (voting_id, voter_id, target_member_id),

    CONSTRAINT chk_votes_stars_range
        CHECK (stars >= 1 AND stars <= 6),

    CONSTRAINT chk_votes_no_self_vote
        CHECK (voter_id != target_member_id)
);

CREATE INDEX idx_votes_voting     ON votes (voting_id);
CREATE INDEX idx_votes_target     ON votes (voting_id, target_member_id);
CREATE INDEX idx_votes_voter      ON votes (voting_id, voter_id);

COMMENT ON TABLE votes IS 'Votos de habilidade — anônimos entre players; sem auto-voto (RN-VOT-002, RN-VOT-003) — BC: Skill Voting';


CREATE TABLE voting_bans (
    id        UUID                     NOT NULL DEFAULT gen_random_uuid(),
    voting_id UUID                     NOT NULL,
    group_id  UUID                     NOT NULL,
    member_id UUID                     NOT NULL,
    reason    TEXT                     NOT NULL,
    banned_by UUID                     NOT NULL,
    banned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_voting_bans PRIMARY KEY (id),

    CONSTRAINT fk_voting_bans_voting
        FOREIGN KEY (voting_id) REFERENCES skill_votings (id),

    CONSTRAINT fk_voting_bans_member
        FOREIGN KEY (member_id) REFERENCES group_members (id),

    CONSTRAINT fk_voting_bans_banner
        FOREIGN KEY (banned_by) REFERENCES users (id),

    CONSTRAINT uq_voting_bans_voting_member
        UNIQUE (voting_id, member_id)
);

CREATE INDEX idx_voting_bans_voting ON voting_bans (voting_id);

COMMENT ON TABLE voting_bans IS 'Jogadores banidos de uma votação específica — BC: Skill Voting';
