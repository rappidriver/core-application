package com.rappidrive.infrastructure.messaging;

import com.rappidrive.application.ports.output.EventDispatcherPort;
import com.rappidrive.application.ports.output.OutboxRepositoryPort;
import com.rappidrive.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class OutboxPublisherTest {

    private OutboxRepositoryPort repo;
    private EventDispatcherPort dispatcher;
    private OutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        repo = mock(OutboxRepositoryPort.class);
        dispatcher = mock(EventDispatcherPort.class);
        publisher = new OutboxPublisher(repo, dispatcher);
    }

    @Test
    void publishPending_success_marksSent() throws Exception {
        OutboxEvent e = new OutboxEvent(UUID.randomUUID(), UUID.randomUUID(), "T", "{}", "PENDING", 0, null, LocalDateTime.now(), null, null);
        when(repo.findPendingBatch(any(LocalDateTime.class), anyInt())).thenReturn(List.of(e));

        publisher.publishPending();

        verify(dispatcher).dispatch(e.getId(), e.getEventType(), e.getPayload());
        verify(repo).markSent(e.getId());
    }

    @Test
    void publishPending_failure_incrementsAttempts() throws Exception {
        OutboxEvent e = new OutboxEvent(UUID.randomUUID(), UUID.randomUUID(), "T", "{}", "PENDING", 0, null, LocalDateTime.now(), null, null);
        when(repo.findPendingBatch(any(LocalDateTime.class), anyInt())).thenReturn(List.of(e));
        doThrow(new RuntimeException("fail")).when(dispatcher).dispatch(any(), any(), any());

        publisher.publishPending();

        ArgumentCaptor<java.time.LocalDateTime> cap = ArgumentCaptor.forClass(java.time.LocalDateTime.class);
        verify(repo).incrementAttempts(eq(e.getId()), cap.capture());
        assertThat(cap.getValue()).isAfter(LocalDateTime.now().minusSeconds(1));
    }

    @Test
    void publishPending_exceedsMaxAttempts_marksFailed() throws Exception {
        // simulate event with attempts = max-1 (OutboxPublisher maxAttempts=5)
        OutboxEvent e = new OutboxEvent(UUID.randomUUID(), UUID.randomUUID(), "T", "{}", "PENDING", 4, null, LocalDateTime.now(), null, null);
        when(repo.findPendingBatch(any(LocalDateTime.class), anyInt())).thenReturn(List.of(e));
        doThrow(new RuntimeException("fail")).when(dispatcher).dispatch(any(), any(), any());

        publisher.publishPending();

        verify(repo).markFailed(e.getId());
    }
}
