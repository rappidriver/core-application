package com.rappidrive.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when an admin approves a driver application.
 * 
 * This event signals that the driver has been activated on the platform
 * and can now accept trips.
 */
public record DriverApprovedEvent(
    String eventId,
    LocalDateTime occurredOn,
    UUID driverId,
    UUID approvalRequestId,
    UUID approvedByAdminId,
    String approverName
) implements DomainEvent {
}
