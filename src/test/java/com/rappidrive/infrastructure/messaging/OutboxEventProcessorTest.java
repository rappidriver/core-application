package com.rappidrive.infrastructure.messaging;

import com.rappidrive.application.ports.output.EventDispatcherPort;
import com.rappidrive.domain.outbox.OutboxEvent;
import com.rappidrive.infrastructure.persistence.adapters.JpaOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OutboxEventProcessorTest {
    private JpaOutboxRepository outboxRepository;
    private EventDispatcherPort eventDispatcher;
    private OutboxEventProcessor processor;

    @BeforeEach
    void setUp() {
        outboxRepository = mock(JpaOutboxRepository.class);
        eventDispatcher = mock(EventDispatcherPort.class);
        processor = new OutboxEventProcessor(outboxRepository, eventDispatcher);
    }

    @Test
    void shouldProcessPendingEventsSuccessfully() throws Exception {
        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(), UUID.randomUUID(), "TRIP_CREATED", "{}", "PENDING", 0, null, LocalDateTime.now()
        );
        when(outboxRepository.findPendingForUpdate(anyInt())).thenReturn(List.of(event));

        processor.processPendingEvents();

        verify(eventDispatcher, times(1)).dispatch(event.getId(), event.getEventType(), event.getPayload());
        verify(outboxRepository, atLeastOnce()).save(any());
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void shouldMarkEventAsFailedAfterMaxRetries() throws Exception {
        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(), UUID.randomUUID(), "TRIP_CREATED", "{}", "PENDING", 4, null, LocalDateTime.now()
        );
        when(outboxRepository.findPendingForUpdate(anyInt())).thenReturn(List.of(event));
        doThrow(new RuntimeException("Dispatch error")).when(eventDispatcher).dispatch(event.getId(), event.getEventType(), event.getPayload());

        processor.processPendingEvents();

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().stream().anyMatch(e -> e.getAttempts() >= 5)).isTrue();
        assertThat(MDC.get("correlationId")).isNull();
    }
}
