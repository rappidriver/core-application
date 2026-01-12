package com.rappidrive.domain.outbox;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain value object representing an outbox event record.
 */
public final class OutboxEvent {
    private final UUID id;
    private final UUID aggregateId;
    private final String eventType;
    private final String payload;
    private final String status;
    private final int attempts;
    private final LocalDateTime nextAttemptAt;
    private final LocalDateTime createdAt;
    private final String traceId;
    private final String spanId;

    public OutboxEvent(UUID id, UUID aggregateId, String eventType, String payload,
                       String status, int attempts, LocalDateTime nextAttemptAt, LocalDateTime createdAt,
                       String traceId, String spanId) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.attempts = attempts;
        this.nextAttemptAt = nextAttemptAt;
        this.createdAt = createdAt;
        this.traceId = traceId;
        this.spanId = spanId;
    }

    public UUID getId() { return id; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public String getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public LocalDateTime getNextAttemptAt() { return nextAttemptAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getTraceId() { return traceId; }
    public String getSpanId() { return spanId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutboxEvent that = (OutboxEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
