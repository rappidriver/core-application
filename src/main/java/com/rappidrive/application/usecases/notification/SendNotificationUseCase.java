package com.rappidrive.application.usecases.notification;

import com.rappidrive.application.metrics.DriverAssignmentAttemptStatus;
import com.rappidrive.application.metrics.DriverAssignmentStage;
import com.rappidrive.application.ports.input.notification.SendNotificationInputPort;
import com.rappidrive.application.ports.output.DriverAssignmentMetricsPort;
import com.rappidrive.application.ports.output.NotificationRepositoryPort;
import com.rappidrive.application.ports.output.NotificationServicePort;
import com.rappidrive.application.ports.output.TelemetryPort;
import com.rappidrive.domain.entities.Notification;
import com.rappidrive.domain.valueobjects.NotificationContent;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SendNotificationUseCase implements SendNotificationInputPort {
    
    private final NotificationRepositoryPort notificationRepository;
    private final NotificationServicePort notificationService;
    private final TelemetryPort telemetryPort;
    private final DriverAssignmentMetricsPort metricsPort;
    
    public SendNotificationUseCase(
            NotificationRepositoryPort notificationRepository,
            NotificationServicePort notificationService,
            TelemetryPort telemetryPort,
            DriverAssignmentMetricsPort metricsPort) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.telemetryPort = telemetryPort;
        this.metricsPort = metricsPort;
    }
    
    @Override
    public Notification execute(SendNotificationCommand command) {
        Map<String, String> attributes = telemetryAttributes(command);
        return telemetryPort.traceUseCase("driver.notify", attributes, () -> executeWithMetrics(command));
    }

    private Notification executeWithMetrics(SendNotificationCommand command) {
        long start = System.nanoTime();
        try {
            Notification notification = doSend(command);
            metricsPort.incrementAttempts(DriverAssignmentStage.NOTIFICATION, DriverAssignmentAttemptStatus.SUCCESS);
            return notification;
        } catch (RuntimeException ex) {
            metricsPort.incrementAttempts(DriverAssignmentStage.NOTIFICATION, DriverAssignmentAttemptStatus.ERROR);
            throw ex;
        } finally {
            metricsPort.recordStageDuration(DriverAssignmentStage.NOTIFICATION, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
        }
    }

    private Notification doSend(SendNotificationCommand command) {
        if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
            Optional<Notification> existing = notificationRepository
                .findByIdempotencyKey(command.idempotencyKey(), command.tenantId());
            
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        
        NotificationContent content = NotificationContent.of(
            command.title(),
            command.message(),
            command.data()
        );
        
        Notification notification = Notification.create(
            command.userId(),
            command.type(),
            content,
            command.tenantId(),
            command.idempotencyKey()
        );
        
        Notification savedNotification = notificationRepository.save(notification);
        
        if (savedNotification.getPriority().shouldSendPush() && notificationService.isAvailable()) {
            boolean sent = notificationService.sendPushNotification(savedNotification);
            if (sent) {
                savedNotification.markAsSent();
                notificationRepository.save(savedNotification);
            } else {
                savedNotification.markAsFailed();
                notificationRepository.save(savedNotification);
            }
        }
        
        return savedNotification;
    }

    private Map<String, String> telemetryAttributes(SendNotificationCommand command) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("stage", "driver.notify");
        attributes.put("tenantId", command.tenantId() != null ? command.tenantId().asString() : "unknown");
        attributes.put("tripId", resolveTripId(command));
        return attributes;
    }

    private String resolveTripId(SendNotificationCommand command) {
        if (command.data() == null) {
            return "unknown";
        }
        if (command.data().containsKey("tripId")) {
            return Optional.ofNullable(command.data().get("tripId"))
                .filter(value -> !value.isBlank())
                .orElse("unknown");
        }
        if (command.data().containsKey("trip_id")) {
            return Optional.ofNullable(command.data().get("trip_id"))
                .filter(value -> !value.isBlank())
                .orElse("unknown");
        }
        return "unknown";
    }
}
