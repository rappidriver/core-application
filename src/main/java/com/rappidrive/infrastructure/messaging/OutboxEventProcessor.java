package com.rappidrive.infrastructure.messaging;

import com.rappidrive.application.ports.output.EventDispatcherPort;
import com.rappidrive.application.ports.output.OutboxRepositoryPort;
import com.rappidrive.domain.outbox.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Outbox Event Processor - Relay mechanism for processing domain events.
 *
 * This component:
 * 1. Periodically polls the outbox_event table for pending events
 * 2. Dispatches each event via EventDispatcherPort
 * 3. Marks events as PROCESSED or FAILED based on outcome
 * 4. Retries failed events with exponential backoff
 * 5. Provides structured logging with correlationId for tracing
 *
 * HIST-2026-011: Outbox Relay Implementation
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "outbox.processor.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class OutboxEventProcessor {

    private final OutboxRepositoryPort outboxRepository;
    private final EventDispatcherPort eventDispatcher;

    private static final int MAX_RETRIES = 5;
    private static final int DEFAULT_BATCH_SIZE = 50;

    /**
     * Process pending outbox events.
     * Runs on a fixed delay (configurable via ${outbox.processor.delay-ms})
     *
     * Uses pessimistic locking (SELECT ... FOR UPDATE SKIP LOCKED) to ensure
     * concurrent instances don't process same events.
     */
    @Scheduled(fixedDelayString = "${outbox.processor.delay-ms:1000}")
    @Transactional
    public void processPendingEvents() {
        try {
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            List<OutboxEvent> events = outboxRepository.findPendingBatch(now, DEFAULT_BATCH_SIZE);

            if (events.isEmpty()) {
                log.debug("[OUTBOX_IDLE] No pending events");
                return;
            }

            log.info("[OUTBOX_START] processing={} pending events", events.size());

            int successCount = 0;
            int failureCount = 0;

            for (OutboxEvent event : events) {
                MDC.put("correlationId", event.getId().toString());
                try {
                    dispatchEvent(event);
                    outboxRepository.markSent(event.getId());
                    successCount++;
                    log.info("[OUTBOX_SUCCESS] eventId={} type={} attempt={}/{}", 
                             event.getId(), event.getEventType(), event.getAttempts() + 1, MAX_RETRIES);
                } catch (Exception ex) {
                    failureCount++;
                    handleEventFailure(event, ex);
                } finally {
                    MDC.remove("correlationId");
                }
            }

            log.info("[OUTBOX_COMPLETE] success={} failed={} total={}", 
                     successCount, failureCount, events.size());

        } catch (Exception ex) {
            log.error("[OUTBOX_ERROR] Unexpected error processing outbox events", ex);
        }
    }

    /**
     * Dispatch a single event via EventDispatcherPort.
     * Wraps exceptions for proper retry handling.
     */
    private void dispatchEvent(OutboxEvent event) {
        try {
            eventDispatcher.dispatch(event.getId(), event.getEventType(), event.getPayload());
        } catch (Exception ex) {
            throw new OutboxDispatchException("Failed to dispatch event: " + event.getId(), ex);
        }
    }

    /**
     * Handle event failure with retry logic.
     * Increments attempt counter and marks as FAILED if max retries exceeded.
     */
    private void handleEventFailure(OutboxEvent event, Exception ex) {
        int newAttempt = event.getAttempts() + 1;

        if (newAttempt >= MAX_RETRIES) {
            outboxRepository.markFailed(event.getId());
            log.error("[OUTBOX_FAILED] eventId={} type={} attempts_exceeded={}/{}", 
                     event.getId(), event.getEventType(), newAttempt, MAX_RETRIES, ex);
        } else {
            // Calculate next attempt time with exponential backoff
            LocalDateTime nextAttemptAt = calculateNextAttemptTime(newAttempt);
            outboxRepository.incrementAttempts(event.getId(), nextAttemptAt);
            log.warn("[OUTBOX_RETRY] eventId={} type={} attempt={}/{} nextAttempt={}", 
                    event.getId(), event.getEventType(), newAttempt, MAX_RETRIES, nextAttemptAt);
        }
    }

    /**
     * Calculate next attempt time with exponential backoff.
     * Formula: 2^attempt * 100ms (capped at 30 seconds)
     */
    private LocalDateTime calculateNextAttemptTime(int attempt) {
        long delaySeconds = Math.min((long) Math.pow(2, attempt) * 100, 30_000) / 1000;
        return LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(delaySeconds);
    }

    /**
     * Custom exception for outbox dispatch failures.
     */
    public static class OutboxDispatchException extends RuntimeException {
        public OutboxDispatchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

