-- ========================================================
-- Flyway Migration: V1__create_initial_schema.sql
-- Crea la tabla 'data_records' usada por DataRecord.java
-- ========================================================

CREATE TABLE IF NOT EXISTS data_records (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    author_name VARCHAR(255),
    source_url VARCHAR(500),
    category VARCHAR(100),
    external_id VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Índice único para evitar duplicados en ETL
CREATE UNIQUE INDEX IF NOT EXISTS idx_data_records_external_id ON data_records(external_id);

-- Índices adicionales para mejorar consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_data_records_category ON data_records(category);
CREATE INDEX IF NOT EXISTS idx_data_records_author ON data_records(author_name);
CREATE INDEX IF NOT EXISTS idx_data_records_created_at ON data_records(created_at);
