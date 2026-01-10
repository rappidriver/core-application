package com.rappidrive.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when an admin rejects a driver application.
 * 
 * This event signals that the driver's application was denied.
 * The permanentBan flag indicates whether the driver can resubmit
 * their application later.
 */
public record DriverRejectedEvent(
    String eventId,
    LocalDateTime occurredOn,
    UUID driverId,
    UUID approvalRequestId,
    UUID rejectedByAdminId,
    String rejectionReason,
    boolean permanentBan
) implements DomainEvent {
}
