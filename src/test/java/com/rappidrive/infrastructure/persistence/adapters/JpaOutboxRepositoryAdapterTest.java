package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.domain.outbox.OutboxEvent;
import com.rappidrive.infrastructure.persistence.entities.OutboxEventJpaEntity;
import com.rappidrive.infrastructure.persistence.repositories.SpringDataOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JpaOutboxRepositoryTest {

    private SpringDataOutboxRepository repo;
    private com.rappidrive.infrastructure.persistence.adapters.JpaOutboxRepository adapter;

    @BeforeEach
    void setUp() {
        repo = mock(SpringDataOutboxRepository.class);
        adapter = new com.rappidrive.infrastructure.persistence.adapters.JpaOutboxRepository(repo);
    }

    @Test
    void save_persistsEntity() {
        com.rappidrive.domain.outbox.OutboxEvent evt = new com.rappidrive.domain.outbox.OutboxEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "T",
            "{\"a\":1}",
            "PENDING",
            0,
            null,
            LocalDateTime.now(),
            null,
            null
        );

        adapter.save(evt);

        ArgumentCaptor<OutboxEventJpaEntity> cap = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
        verify(repo).save(cap.capture());
        OutboxEventJpaEntity saved = cap.getValue();
        assertThat(saved.getEventType()).isEqualTo("T");
        assertThat(saved.getPayload()).isEqualTo("{\"a\":1}");
    }

    @Test
    void findPendingBatch_mapsEntitiesToDomain() {
        OutboxEventJpaEntity e = new OutboxEventJpaEntity(UUID.randomUUID(), UUID.randomUUID(), "T", "{}", "PENDING", 0, null, LocalDateTime.now(), null, null);
        when(repo.findPending(any(LocalDateTime.class), any())).thenReturn(List.of(e));

        List<OutboxEvent> batch = adapter.findPendingBatch(LocalDateTime.now(), 10);

        assertThat(batch).hasSize(1);
        assertThat(batch.get(0).getEventType()).isEqualTo("T");
    }

    @Test
    void markSent_updatesEntity() {
        UUID id = UUID.randomUUID();
        OutboxEventJpaEntity e = new OutboxEventJpaEntity();
        e.setId(id);
        when(repo.findById(id)).thenReturn(Optional.of(e));

        adapter.markSent(id);

        verify(repo).save(e);
        assertThat(e.getStatus()).isEqualTo("SENT");
        assertThat(e.getSentAt()).isNotNull();
    }
}
