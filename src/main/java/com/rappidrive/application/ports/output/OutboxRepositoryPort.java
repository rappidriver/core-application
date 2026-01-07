package com.rappidrive.application.ports.output;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rappidrive.domain.outbox.OutboxEvent;

public interface OutboxRepositoryPort {

    void save(OutboxEvent event);

    List<OutboxEvent> findPendingBatch(LocalDateTime now, int limit);

    void markSent(UUID id);

    void incrementAttempts(UUID id, LocalDateTime nextAttemptAt);

    void markFailed(UUID id);

    Optional<OutboxEvent> findById(UUID id);
}
