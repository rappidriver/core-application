-- V8: Create notifications table
-- Description: Tabela para armazenar notificações push e in-app enviadas aos usuários

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    message VARCHAR(500) NOT NULL,
    data JSONB,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(255),
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    failed_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_notifications_type CHECK (type IN (
        'TRIP_REQUEST', 'TRIP_ACCEPTED', 'TRIP_STARTED', 'TRIP_COMPLETED', 'TRIP_CANCELLED',
        'PAYMENT_PROCESSED', 'PAYMENT_FAILED',
        'RATING_RECEIVED',
        'PROMOTION', 'SYSTEM_ALERT'
    )),
    CONSTRAINT chk_notifications_priority CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT chk_notifications_status CHECK (status IN ('PENDING', 'SENT', 'READ', 'FAILED')),
    CONSTRAINT uk_notifications_idempotency_key UNIQUE (idempotency_key)
);

-- Índices para performance
CREATE INDEX idx_notifications_user_id_status ON notifications (user_id, status);
CREATE INDEX idx_notifications_tenant_id ON notifications (tenant_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at DESC);
CREATE INDEX idx_notifications_type_tenant ON notifications (type, tenant_id);
CREATE INDEX idx_notifications_idempotency_key ON notifications (idempotency_key) WHERE idempotency_key IS NOT NULL;

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_notifications_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_notifications_updated_at
    BEFORE UPDATE ON notifications
    FOR EACH ROW
    EXECUTE FUNCTION update_notifications_updated_at();

-- Comentários na tabela e colunas
COMMENT ON TABLE notifications IS 'Armazena notificações push e in-app enviadas aos usuários';
COMMENT ON COLUMN notifications.id IS 'Identificador único da notificação';
COMMENT ON COLUMN notifications.user_id IS 'ID do usuário destinatário da notificação';
COMMENT ON COLUMN notifications.type IS 'Tipo de notificação (TRIP_REQUEST, PAYMENT_PROCESSED, etc)';
COMMENT ON COLUMN notifications.priority IS 'Prioridade (HIGH: push+som, MEDIUM: push, LOW: in-app)';
COMMENT ON COLUMN notifications.title IS 'Título da notificação (max 100 chars)';
COMMENT ON COLUMN notifications.message IS 'Mensagem descritiva (max 500 chars)';
COMMENT ON COLUMN notifications.data IS 'Dados adicionais em JSON (deeplinks, IDs de entidades, etc)';
COMMENT ON COLUMN notifications.status IS 'Status (PENDING: criada, SENT: enviada, READ: lida, FAILED: falhou)';
COMMENT ON COLUMN notifications.idempotency_key IS 'Chave para evitar duplicatas de notificações (UNIQUE)';
COMMENT ON COLUMN notifications.tenant_id IS 'ID do tenant (multi-tenancy)';
COMMENT ON COLUMN notifications.created_at IS 'Data de criação da notificação';
COMMENT ON COLUMN notifications.sent_at IS 'Data de envio do push notification';
COMMENT ON COLUMN notifications.read_at IS 'Data em que usuário leu a notificação (in-app)';
COMMENT ON COLUMN notifications.failed_at IS 'Data de falha no envio';
COMMENT ON COLUMN notifications.deleted_at IS 'Soft-delete para limpeza de notificações antigas';
COMMENT ON COLUMN notifications.updated_at IS 'Data da última atualização';
