-- V7: Criar tabela de avaliações (ratings)
-- Data: 2026-01-03
-- Descrição: Sistema de avaliações bidirecional (passageiro ↔ motorista)

-- =============================================
-- 1. Criar tabela ratings
-- =============================================
CREATE TABLE ratings (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL,
    rater_id UUID NOT NULL,
    ratee_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('DRIVER_BY_PASSENGER', 'PASSENGER_BY_DRIVER')),
    score INTEGER NOT NULL CHECK (score >= 1 AND score <= 5),
    comment VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'REPORTED', 'DELETED')),
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_ratings_trip FOREIGN KEY (trip_id) REFERENCES trips(id),
    CONSTRAINT uq_ratings_trip_rater_type UNIQUE (trip_id, rater_id, type),
    CONSTRAINT chk_ratings_different_users CHECK (rater_id != ratee_id)
);

-- =============================================
-- 2. Criar índices para performance
-- =============================================
CREATE INDEX idx_ratings_trip_id ON ratings(trip_id);
CREATE INDEX idx_ratings_ratee_type_status ON ratings(ratee_id, type, status);
CREATE INDEX idx_ratings_tenant_id ON ratings(tenant_id);
CREATE INDEX idx_ratings_created_at ON ratings(created_at);

-- =============================================
-- 3. Comentários nas tabelas e colunas
-- =============================================
COMMENT ON TABLE ratings IS 'Avaliações de motoristas e passageiros após viagens concluídas';

COMMENT ON COLUMN ratings.id IS 'Identificador único da avaliação';
COMMENT ON COLUMN ratings.trip_id IS 'ID da viagem avaliada';
COMMENT ON COLUMN ratings.rater_id IS 'ID de quem avalia (passageiro ou motorista)';
COMMENT ON COLUMN ratings.ratee_id IS 'ID de quem é avaliado (motorista ou passageiro)';
COMMENT ON COLUMN ratings.type IS 'Tipo de avaliação: DRIVER_BY_PASSENGER (passageiro avalia motorista) ou PASSENGER_BY_DRIVER (motorista avalia passageiro)';
COMMENT ON COLUMN ratings.score IS 'Nota de 1 a 5 estrelas';
COMMENT ON COLUMN ratings.comment IS 'Comentário opcional (máximo 500 caracteres)';
COMMENT ON COLUMN ratings.status IS 'Status da avaliação: ACTIVE (ativa), REPORTED (reportada como ofensiva), DELETED (soft-deleted)';
COMMENT ON COLUMN ratings.tenant_id IS 'ID do tenant (multi-tenancy)';
COMMENT ON COLUMN ratings.created_at IS 'Data/hora de criação da avaliação';
COMMENT ON COLUMN ratings.updated_at IS 'Data/hora da última atualização';

-- =============================================
-- 4. Trigger para updated_at
-- =============================================
CREATE OR REPLACE FUNCTION update_ratings_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_ratings_updated_at
    BEFORE UPDATE ON ratings
    FOR EACH ROW
    EXECUTE FUNCTION update_ratings_updated_at();

-- =============================================
-- 5. Grants (opcional, se necessário)
-- =============================================
-- GRANT SELECT, INSERT, UPDATE ON ratings TO rappidrive_app;
