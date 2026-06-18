ALTER TABLE team_players ALTER COLUMN member_id DROP NOT NULL;
ALTER TABLE team_players ADD COLUMN guest_id UUID REFERENCES match_guests (id);
ALTER TABLE team_players ADD CONSTRAINT chk_team_players_subject
    CHECK (
        (member_id IS NOT NULL AND guest_id IS NULL) OR
        (member_id IS NULL     AND guest_id IS NOT NULL)
    );
