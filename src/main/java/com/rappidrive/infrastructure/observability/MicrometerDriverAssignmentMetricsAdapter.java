package com.rappidrive.infrastructure.observability;

import com.rappidrive.application.metrics.DriverAssignmentAttemptStatus;
import com.rappidrive.application.metrics.DriverAssignmentStage;
import com.rappidrive.application.ports.output.DriverAssignmentMetricsPort;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MicrometerDriverAssignmentMetricsAdapter implements DriverAssignmentMetricsPort {

    private static final String DURATION_METRIC = "driver_assignment_duration_seconds";
    private static final String ATTEMPTS_METRIC = "driver_assignment_attempts_total";
    private static final String QUEUE_METRIC = "driver_assignment_queue_size";

    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> timersByStage = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> queueGaugeByTenant = new ConcurrentHashMap<>();

    public MicrometerDriverAssignmentMetricsAdapter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordStageDuration(DriverAssignmentStage stage, long durationMillis) {
        resolveTimer(stage).record(durationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void incrementAttempts(DriverAssignmentStage stage, DriverAssignmentAttemptStatus status) {
        meterRegistry.counter(
            ATTEMPTS_METRIC,
            "stage", normalize(stage),
            "status", status.name().toLowerCase()
        ).increment();
    }

    @Override
    public void incrementQueue(String tenantId) {
        queueGauge(tenantId).incrementAndGet();
    }

    @Override
    public void decrementQueue(String tenantId) {
        queueGauge(tenantId).updateAndGet(current -> Math.max(0, current - 1));
    }

    private Timer resolveTimer(DriverAssignmentStage stage) {
        return timersByStage.computeIfAbsent(stage.name(), key ->
            Timer.builder(DURATION_METRIC)
                .description("Driver assignment stage duration")
                .tags("stage", normalize(stage))
                .publishPercentiles(0.5, 0.9, 0.99)
                .publishPercentileHistogram()
                .register(meterRegistry)
        );
    }

    private AtomicInteger queueGauge(String tenantId) {
        String tenantTag = tenantId == null || tenantId.isBlank() ? "unknown" : tenantId;
        return queueGaugeByTenant.computeIfAbsent(tenantTag, key -> {
            AtomicInteger gauge = new AtomicInteger(0);
            meterRegistry.gauge(QUEUE_METRIC, Tags.of("tenant", tenantTag), gauge);
            return gauge;
        });
    }

    private String normalize(DriverAssignmentStage stage) {
        return stage.name().toLowerCase();
    }
}
