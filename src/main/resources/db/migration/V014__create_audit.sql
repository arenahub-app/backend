-- ─────────────────────────────────────────────────────────────────────────────
-- Função e triggers de auditoria de updated_at
-- Garante que updated_at seja atualizado em qualquer UPDATE, inclusive os
-- feitos diretamente via SQL (fora do ORM).
-- ─────────────────────────────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- users
CREATE TRIGGER trg_updated_at_users
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- groups
CREATE TRIGGER trg_updated_at_groups
    BEFORE UPDATE ON groups
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- group_members
CREATE TRIGGER trg_updated_at_group_members
    BEFORE UPDATE ON group_members
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- referee_profiles
CREATE TRIGGER trg_updated_at_referee_profiles
    BEFORE UPDATE ON referee_profiles
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- matches
CREATE TRIGGER trg_updated_at_matches
    BEFORE UPDATE ON matches
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- presence_entries
CREATE TRIGGER trg_updated_at_presence_entries
    BEFORE UPDATE ON presence_entries
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- votes
CREATE TRIGGER trg_updated_at_votes
    BEFORE UPDATE ON votes
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- referee_profiles (already above)

-- charges
CREATE TRIGGER trg_updated_at_charges
    BEFORE UPDATE ON charges
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
