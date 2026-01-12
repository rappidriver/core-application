ALTER TABLE outbox_event
    ADD COLUMN IF NOT EXISTS trace_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS span_id VARCHAR(32);

CREATE INDEX IF NOT EXISTS idx_outbox_trace ON outbox_event(trace_id, span_id);
