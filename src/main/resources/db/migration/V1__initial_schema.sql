-- Flyway migration: Initial schema for Alerta Enchentes
-- Version: 1.0.0

CREATE TABLE IF NOT EXISTS pontos_monitoramento (
    id VARCHAR(50) PRIMARY KEY,
    bairro VARCHAR(100) NOT NULL,
    ponto VARCHAR(200),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    ts TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS alertas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nivel VARCHAR(20) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    ts TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS relatos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bairro VARCHAR(100),
    tipo VARCHAR(50),
    descricao TEXT NOT NULL,
    foto TEXT,
    ts TIMESTAMP NOT NULL,
    local_only BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_pontos_bairro ON pontos_monitoramento(bairro);
CREATE INDEX IF NOT EXISTS idx_pontos_status ON pontos_monitoramento(status);
CREATE INDEX IF NOT EXISTS idx_alertas_bairro ON alertas(bairro);
CREATE INDEX IF NOT EXISTS idx_alertas_ts ON alertas(ts DESC);
CREATE INDEX IF NOT EXISTS idx_alertas_nivel ON alertas(nivel);
CREATE INDEX IF NOT EXISTS idx_relatos_bairro ON relatos(bairro);
CREATE INDEX IF NOT EXISTS idx_relatos_ts ON relatos(ts DESC);
CREATE INDEX IF NOT EXISTS idx_relatos_local_only ON relatos(local_only);

-- Insert default monitoring points for Itajubá/MG
INSERT INTO pontos_monitoramento (id, bairro, ponto, latitude, longitude, status, ts) VALUES
('varginha', 'Varginha', 'Rio Sapucaí', -22.44, -45.45, 'NORMAL', CURRENT_TIMESTAMP),
('medicina', 'Medicina', 'Ribeirão José Pereira', -22.42, -45.45, 'NORMAL', CURRENT_TIMESTAMP),
('sao-sebastiao', 'São Sebastião', 'Ribeirão Anhumas', -22.41, -45.43, 'NORMAL', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;