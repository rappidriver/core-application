-- Flyway migration: Create outbox table
CREATE TABLE IF NOT EXISTS outbox_event (
  id UUID PRIMARY KEY,
  aggregate_id UUID,
  event_type VARCHAR(255) NOT NULL,
  payload JSONB NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  attempts INT NOT NULL DEFAULT 0,
  next_attempt_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  sent_at TIMESTAMP NULL
);
CREATE INDEX IF NOT EXISTS idx_outbox_status_next_attempt ON outbox_event(status, next_attempt_at);
