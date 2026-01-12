package com.rappidrive.infrastructure.observability;

import com.rappidrive.application.ports.output.TelemetryPort;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Framework adapter that bridges the TelemetryPort with Micrometer observations/metrics.
 */
@Component
public class MicrometerTelemetryAdapter implements TelemetryPort {

    private final ObservationRegistry observationRegistry;
    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();

    public MicrometerTelemetryAdapter(ObservationRegistry observationRegistry, MeterRegistry meterRegistry) {
        this.observationRegistry = observationRegistry;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public <T> T traceUseCase(String useCaseName, Map<String, String> attributes, Supplier<T> supplier) {
        String observationName = observationName(useCaseName);
        Observation observation = Observation.start(observationName, observationRegistry);
        attributes.forEach(observation::highCardinalityKeyValue);
        Timer.Sample sample = Timer.start(meterRegistry);

        try (Observation.Scope ignored = observation.openScope()) {
            T result = supplier.get();
            incrementCounter(useCaseName, "success");
            return result;
        } catch (RuntimeException ex) {
            incrementCounter(useCaseName, "error");
            observation.error(ex);
            throw ex;
        } finally {
            sample.stop(resolveTimer(useCaseName));
            observation.stop();
        }
    }

    private void incrementCounter(String useCaseName, String status) {
        meterRegistry.counter(
            "usecase_execution_total",
            "usecase", useCaseName,
            "status", status
        ).increment();
    }

    private Timer resolveTimer(String useCaseName) {
        return timers.computeIfAbsent(useCaseName, name ->
            Timer.builder("usecase_execution_duration")
                .description("Use case execution duration")
                .tags("usecase", name)
                .publishPercentileHistogram()
                .register(meterRegistry)
        );
    }

    private String observationName(String useCaseName) {
        return useCaseName.startsWith("usecase.") ? useCaseName : "usecase." + useCaseName;
    }
}
