package com.rappidrive.infrastructure.messaging;

import com.rappidrive.application.ports.output.EventDispatcherPort;
import com.rappidrive.domain.outbox.OutboxEvent;
import com.rappidrive.infrastructure.persistence.adapters.JpaOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {
    private final JpaOutboxRepository outboxRepository;
    private final EventDispatcherPort eventDispatcher;
    private static final int MAX_RETRIES = 5;
    private static final int BATCH_SIZE = 20;

    @Scheduled(fixedDelayString = "${outbox.processor.delay:2000}")
    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findPendingForUpdate(BATCH_SIZE);
        for (OutboxEvent event : events) {
            try {
                MDC.put("correlationId", event.getId().toString());
                log.info("[Outbox] Processing event {} type={} attempt={}", event.getId(), event.getEventType(), event.getAttempts());
                eventDispatcher.dispatch(event.getId(), event.getEventType(), event.getPayload());
                // Aqui você deve atualizar o status/processamento do evento conforme seu modelo
                outboxRepository.save(event);
                log.info("[Outbox] Event {} processed successfully", event.getId());
            } catch (Exception ex) {
                // Aqui você deve incrementar tentativas e marcar como failed se necessário
                int newAttempts = event.getAttempts() + 1;
                boolean failed = newAttempts >= MAX_RETRIES;
                OutboxEvent updated = new OutboxEvent(
                    event.getId(),
                    event.getAggregateId(),
                    event.getEventType(),
                    event.getPayload(),
                    failed ? "FAILED" : event.getStatus(),
                    newAttempts,
                    event.getNextAttemptAt(),
                    event.getCreatedAt()
                );
                outboxRepository.save(updated);
                log.error("[Outbox] Event {} failed: {} (attempt {}/{})", event.getId(), ex.getMessage(), newAttempts, MAX_RETRIES);
            } finally {
                MDC.remove("correlationId");
            }
        }
    }
}
