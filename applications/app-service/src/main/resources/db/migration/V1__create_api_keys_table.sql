-- Tabla para almacenar API Keys
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
    description  VARCHAR(500),
    allowed_ip   VARCHAR(45)  NULL
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_api_keys_enabled ON api_keys (enabled);
CREATE INDEX idx_api_keys_expires_at ON api_keys (expires_at);
CREATE INDEX idx_api_keys_secret_hash ON api_keys (secret_hash);

-- Insertar API Keys de ejemplo para desarrollo
-- Secret: "dev-secret-key-12345" → bcrypt hash
INSERT INTO api_keys (id, name, secret_hash, enabled, description, created_at, updated_at,
                      expires_at)
VALUES ('dev-client',
        'Cliente de Desarrollo',
        '$2a$10$N9qo8uLOickgx2ZMRZoMye1J9YW4g5FYCFWjL5K3BZqvKLxvvK4.G', -- dev-secret-key-12345
        TRUE,
        'API Key para ambiente de desarrollo local',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        DATEADD('DAY', 365, CURRENT_TIMESTAMP) -- Expira en 1 año
       );

-- Secret: "qa-secret-key-67890" → bcrypt hash
INSERT INTO api_keys (id, name, secret_hash, enabled, description, created_at, updated_at,
                      expires_at)
VALUES ('qa-client',
        'Cliente de QA',
        '$2a$10$Ib5pL3JGqxZHQ8k5xN9e1.vYyF7Q0xT3mH2kW9pR4nS8lM6jN5oO.', -- qa-secret-key-67890
        TRUE,
        'API Key para ambiente de QA/Testing',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        DATEADD('DAY', 180, CURRENT_TIMESTAMP) -- Expira en 6 meses
       );

-- Secret: "claude-secret-key-abcde" → bcrypt hash
INSERT INTO api_keys (id, name, secret_hash, enabled, description, created_at, updated_at,
                      expires_at)
VALUES ('claude-desktop',
        'Claude Desktop App',
        '$2a$10$kH3lR5mN8pQ9wX2yB4vZ1.tU6nM5kJ3fR8sW7qL2pN4mH9vK6xO.', -- claude-secret-key-abcde
        TRUE,
        'API Key para Claude Desktop application',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL -- No expira
       );

-- API Key deshabilitada (para testing)
INSERT INTO api_keys (id, name, secret_hash, enabled, description, created_at, updated_at)
VALUES ('disabled-client',
        'Cliente Deshabilitado',
        '$2a$10$dummyHashForDisabledClient123456789012345678901234',
        FALSE,
        'API Key deshabilitada para pruebas',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);