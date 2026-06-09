CREATE TABLE password_reset_tokens (
    id          UUID                     NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID                     NOT NULL,
    token_hash  VARCHAR(255)             NOT NULL,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at     TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE UNIQUE INDEX uq_password_reset_tokens_hash ON password_reset_tokens (token_hash);
CREATE INDEX idx_password_reset_tokens_user ON password_reset_tokens (user_id);

COMMENT ON TABLE password_reset_tokens IS 'Tokens de redefinição de senha — uso único, expiram em 1h';
COMMENT ON COLUMN password_reset_tokens.token_hash IS 'SHA-256 hex do token opaco enviado por email';
COMMENT ON COLUMN password_reset_tokens.used_at IS 'Preenchido quando o token é usado — impede reutilização';
