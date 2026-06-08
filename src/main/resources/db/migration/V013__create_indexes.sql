-- ─────────────────────────────────────────────────────────────────────────────
-- Índices adicionais de performance
-- Os índices de constraint e hot-path já foram criados em suas respectivas
-- migrations. Este arquivo adiciona índices compostos para queries analíticas
-- e relatórios.
-- ─────────────────────────────────────────────────────────────────────────────

-- Presença: query de "jogadores confirmados por partida" (formação de times)
CREATE INDEX idx_presence_confirmed
    ON presence_entries (match_id)
    WHERE status = 'CONFIRMED';

-- Presença: fila de espera por prazo de notificação (scheduler de expiração)
CREATE INDEX idx_waiting_notified_deadline
    ON waiting_entries (notification_deadline)
    WHERE notified_at IS NOT NULL;

-- Cobranças: inadimplentes (pendentes há mais tempo)
CREATE INDEX idx_charges_pending_group
    ON charges (group_id, created_at)
    WHERE status = 'PENDING';

-- Votação: votos por target para cálculo de média
CREATE INDEX idx_votes_target_stars
    ON votes (voting_id, target_member_id, stars);

-- Financial: saldo do grupo (receitas e despesas ativas)
CREATE INDEX idx_financial_active
    ON financial_entries (group_id, type, amount)
    WHERE status = 'ACTIVE';

-- Membros mensalistas ativos (verificação de presença sem pagamento)
CREATE INDEX idx_group_members_subscribers
    ON group_members (group_id, is_subscriber)
    WHERE is_subscriber = TRUE;

-- Refresh tokens expirados (cleanup job)
CREATE INDEX idx_refresh_tokens_expired
    ON refresh_tokens (expires_at)
    WHERE status = 'ACTIVE';

-- Partidas do scheduler de fechamento de lista (hot path a cada minuto)
CREATE INDEX idx_matches_to_close
    ON matches (list_closes_at, group_id)
    WHERE presence_list_status = 'OPEN' AND status = 'SCHEDULED';
