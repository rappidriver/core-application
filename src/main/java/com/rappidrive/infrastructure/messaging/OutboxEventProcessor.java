package com.rappidrive.infrastructure.messaging;

import com.rappidrive.application.ports.output.EventDispatcherPort;
import com.rappidrive.application.ports.output.OutboxRepositoryPort;
import com.rappidrive.domain.outbox.OutboxEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "outbox.processor.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class OutboxEventProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(OutboxEventProcessor.class);

    private final OutboxRepositoryPort outboxRepository;
    private final EventDispatcherPort eventDispatcher;
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;

    private static final int MAX_RETRIES = 5;
    private static final int DEFAULT_BATCH_SIZE = 50;

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
                if (event.getTraceId() != null) {
                    MDC.put("traceId", event.getTraceId());
                }
                if (event.getSpanId() != null) {
                    MDC.put("spanId", event.getSpanId());
                }
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
                    MDC.remove("traceId");
                    MDC.remove("spanId");
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
        Timer.Sample sample = Timer.start(meterRegistry);
        Span span = startSpan(event);
        Tracer.SpanInScope scope = null;
        try {
            if (span != null) {
                scope = tracer.withSpan(span);
            }
            eventDispatcher.dispatch(event.getId(), event.getEventType(), event.getPayload());
            incrementDispatchCounter(event, "success");
        } catch (Exception ex) {
            incrementDispatchCounter(event, "error");
            if (span != null) {
                span.error(ex);
            }
            throw new OutboxDispatchException("Failed to dispatch event: " + event.getId(), ex);
        } finally {
            sample.stop(resolveDispatchTimer(event));
            if (scope != null) {
                scope.close();
            }
            if (span != null) {
                span.end();
            }
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

    private void incrementDispatchCounter(OutboxEvent event, String status) {
        meterRegistry.counter(
            "outbox_dispatch_total",
            "eventType", event.getEventType(),
            "status", status
        ).increment();
    }

    private Timer resolveDispatchTimer(OutboxEvent event) {
        return Timer.builder("outbox_dispatch_duration")
            .description("Outbox event dispatch latency")
            .tags("eventType", event.getEventType())
            .register(meterRegistry);
    }

    private Span startSpan(OutboxEvent event) {
        if (tracer == null) {
            return null;
        }

        Span.Builder builder = tracer.spanBuilder().name("outbox.dispatch")
            .tag("outbox.event.type", event.getEventType());

        if (event.getTraceId() != null && event.getSpanId() != null) {
            TraceContext.Builder contextBuilder = tracer.traceContextBuilder()
                .traceId(event.getTraceId())
                .spanId(event.getSpanId())
                .sampled(true);
            builder.setParent(contextBuilder.build());
        }

        return builder.start();
    }
}

