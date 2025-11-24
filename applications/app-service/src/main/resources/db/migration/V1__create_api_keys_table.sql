-- applications/app-service/src/main/resources/db/migration/V1__create_api_keys_table.sql
CREATE TABLE IF NOT EXISTS api_keys
(
    id           VARCHAR(100) PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    secret_hash  VARCHAR(255) NOT NULL,
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at   TIMESTAMP    NULL,
    last_used_at TIMESTAMP    NULL,
    usage_count  BIGINT                DEFAULT 0,
    description VARCHAR(500) NULL,
    allowed_ip   VARCHAR(45)  NULL
);

-- Índices (también con comillas)
CREATE INDEX IF NOT EXISTS idx_api_keys_enabled ON api_keys (enabled);
CREATE INDEX IF NOT EXISTS idx_api_keys_expires_at ON api_keys (expires_at);
CREATE INDEX IF NOT EXISTS idx_api_keys_last_used_at ON api_keys (last_used_at);

-- Datos iniciales (idempotentes)
INSERT INTO api_keys (id, name, secret_hash, enabled, description, created_at, updated_at,
                      expires_at)
VALUES ('dev-client',
        'Cliente de Desarrollo',
        '$2a$10$N9qo8uLOickgx2ZMRZoMye1J9YW4g5FYCFWjL5K3BZqvKLxvvK4.G', -- dev-secret-key-12345
        true,
        'API Key para desarrollo local',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP + INTERVAL '365 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO api_keys (id, name, secret_hash, enabled, description, created_at, updated_at,
                      expires_at)
VALUES ('qa-client',
        'Cliente de QA',
        '$2a$10$Ib5pL3JGqxZHQ8k5xN9e1.vYyF7Q0xT3mH2kW9pR4nS8lM6jN5oO.', -- qa-secret-key-67890
        true,
        'API Key para pruebas automatizadas',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP + INTERVAL '180 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO api_keys (id, name, secret_hash, enabled, description, created_at, updated_at)
VALUES ('claude-desktop',
        'Claude Desktop App',
        '$2a$10$kH3lR5mN8pQ9wX2yB4vZ1.tU6nM5kJ3fR8sW7qL2pN4mH9vK6xO.', -- claude-secret-key-abcde
        true,
        'Clave oficial para pruebas con Claude Desktop',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Clave deshabilitada para testing
INSERT INTO api_keys (id, name, secret_hash, enabled, description, created_at, updated_at)
VALUES ('disabled-client',
        'Cliente Deshabilitado',
        '$2a$10$dummyHashForDisabledClient123456789012345678901234',
        false,
        'API Key deshabilitada para pruebas de rechazo',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;