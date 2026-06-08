CREATE TABLE groups (
    id          UUID                     NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(80)              NOT NULL,
    sport       VARCHAR(15)              NOT NULL,
    description TEXT,
    photo_url   VARCHAR(500),
    pix_key     VARCHAR(255),
    status      VARCHAR(10)              NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_groups PRIMARY KEY (id),

    CONSTRAINT chk_groups_sport CHECK (
        sport IN ('FOOTBALL', 'VOLLEYBALL', 'BASKETBALL', 'FUTEVOLEI', 'BEACH_TENNIS', 'OTHER')
    ),

    CONSTRAINT chk_groups_status
        CHECK (status IN ('ACTIVE', 'INACTIVE')),

    CONSTRAINT chk_groups_name_length
        CHECK (char_length(name) >= 3 AND char_length(name) <= 80)
);

CREATE INDEX idx_groups_status ON groups (status) WHERE deleted_at IS NULL;

COMMENT ON TABLE groups IS 'Grupos esportivos — unidade de tenant no sistema multi-tenant — BC: Group Management';
COMMENT ON COLUMN groups.pix_key IS 'Chave Pix do grupo usada na validação automática de comprovantes por IA';
COMMENT ON COLUMN groups.sport IS 'Modalidade esportiva — imutável após primeira formação de times (RN-GRP-002)';
