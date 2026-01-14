-- V17__add_trip_cancellation_fields.sql
-- Adiciona suporte para cancelamento de corridas com motivo e ator
-- Nota: cancelled_at já existe desde V2, apenas adicionamos campos complementares

ALTER TABLE trips ADD COLUMN IF NOT EXISTS cancelled_by VARCHAR(20);
ALTER TABLE trips ADD COLUMN IF NOT EXISTS cancellation_reason_enum VARCHAR(50);
ALTER TABLE trips ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_trips_status_created_at ON trips(tenant_id, status, created_at);
