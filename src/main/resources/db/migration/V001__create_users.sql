CREATE TABLE users (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid(),
    name            VARCHAR(100)             NOT NULL,
    email           VARCHAR(255)             NOT NULL,
    password_hash   VARCHAR(255),
    phone           VARCHAR(20),
    birth_date      DATE,
    photo_url       VARCHAR(500),
    auth_provider   VARCHAR(10)              NOT NULL DEFAULT 'LOCAL',
    google_id       VARCHAR(255),
    email_verified  BOOLEAN                  NOT NULL DEFAULT FALSE,
    active          BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_users PRIMARY KEY (id),

    CONSTRAINT chk_users_auth_provider
        CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),

    CONSTRAINT chk_users_local_has_password
        CHECK (auth_provider != 'LOCAL' OR password_hash IS NOT NULL),

    CONSTRAINT chk_users_google_has_google_id
        CHECK (auth_provider != 'GOOGLE' OR google_id IS NOT NULL),

    CONSTRAINT chk_users_name_length
        CHECK (char_length(name) >= 2 AND char_length(name) <= 100)
);

CREATE UNIQUE INDEX uq_users_email_active
    ON users (email)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_users_google_id
    ON users (google_id)
    WHERE google_id IS NOT NULL AND deleted_at IS NULL;

COMMENT ON TABLE users IS 'Usuários do sistema — BC: Identity & Access';
COMMENT ON COLUMN users.password_hash IS 'NULL para usuários autenticados via Google (auth_provider=GOOGLE)';
COMMENT ON COLUMN users.deleted_at IS 'Soft delete — LGPD: anonimização de dados pessoais';


CREATE TABLE refresh_tokens (
    id          UUID                     NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID                     NOT NULL,
    token_hash  VARCHAR(255)             NOT NULL,
    status      VARCHAR(10)              NOT NULL DEFAULT 'ACTIVE',
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT chk_refresh_tokens_status
        CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED'))
);

CREATE UNIQUE INDEX uq_refresh_tokens_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user_status ON refresh_tokens (user_id, status);

COMMENT ON TABLE refresh_tokens IS 'Tokens de renovação de sessão com rotação — BC: Identity & Access';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'Hash BCrypt do token opaco — nunca armazenar o token em texto plano';
