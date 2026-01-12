package com.rappidrive.application.ports.output;

import com.rappidrive.application.metrics.DriverAssignmentAttemptStatus;
import com.rappidrive.application.metrics.DriverAssignmentStage;

/**
 * Output port for recording driver-assignment specific metrics while keeping
 * the application layer decoupled from Micrometer/Prometheus specifics.
 */
public interface DriverAssignmentMetricsPort {

    void recordStageDuration(DriverAssignmentStage stage, long durationMillis);

    void incrementAttempts(DriverAssignmentStage stage, DriverAssignmentAttemptStatus status);

    void incrementQueue(String tenantId);

    void decrementQueue(String tenantId);
}
