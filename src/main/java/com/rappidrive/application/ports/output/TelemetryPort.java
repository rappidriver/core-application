package com.rappidrive.application.ports.output;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Output port that allows application use cases to emit tracing/metrics events
 * without depending on Micrometer or any framework-specific APIs.
 */
public interface TelemetryPort {

    <T> T traceUseCase(String useCaseName, Map<String, String> attributes, Supplier<T> supplier);

    default <T> T traceUseCase(String useCaseName, Supplier<T> supplier) {
        return traceUseCase(useCaseName, Map.of(), supplier);
    }

    default void traceUseCase(String useCaseName, Runnable action) {
        traceUseCase(useCaseName, Map.of(), () -> {
            action.run();
            return null;
        });
    }
}
