package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.application.ports.output.OutboxRepositoryPort;
import com.rappidrive.domain.outbox.OutboxEvent;
import com.rappidrive.infrastructure.persistence.entities.OutboxEventJpaEntity;
import com.rappidrive.infrastructure.persistence.repositories.SpringDataOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaOutboxRepository implements OutboxRepositoryPort {

    private final SpringDataOutboxRepository repository;

    @Override
    public void save(OutboxEvent event) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity(
            event.getId(),
            event.getAggregateId(),
            event.getEventType(),
            event.getPayload(),
            event.getStatus(),
            event.getAttempts(),
            event.getNextAttemptAt(),
            event.getCreatedAt()
        );
        repository.save(entity);
    }

    @Override
    public List<OutboxEvent> findPendingBatch(LocalDateTime now, int limit) {
        return repository.findPending(now, PageRequest.of(0, limit)).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void markSent(UUID id) {
        repository.findById(id).ifPresent(e -> {
            e.setStatus("SENT");
            e.setSentAt(LocalDateTime.now());
            repository.save(e);
        });
    }

    @Override
    public void incrementAttempts(UUID id, LocalDateTime nextAttemptAt) {
        repository.findById(id).ifPresent(e -> {
            e.setAttempts(e.getAttempts() + 1);
            e.setNextAttemptAt(nextAttemptAt);
            repository.save(e);
        });
    }

    @Override
    public void markFailed(UUID id) {
        repository.findById(id).ifPresent(e -> {
            e.setStatus("FAILED");
            repository.save(e);
        });
    }

    @Override
    public Optional<OutboxEvent> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private OutboxEvent toDomain(OutboxEventJpaEntity e) {
        return new OutboxEvent(
            e.getId(),
            e.getAggregateId(),
            e.getEventType(),
            e.getPayload(),
            e.getStatus(),
            e.getAttempts(),
            e.getNextAttemptAt(),
            e.getCreatedAt()
        );
    }
}
