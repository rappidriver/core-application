package com.rappidrive.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when a driver submits their documentation for approval.
 * 
 * This event signals the start of the approval workflow.
 */
public record DriverApprovalSubmittedEvent(
    String eventId,
    LocalDateTime occurredOn,
    UUID driverId,
    UUID approvalRequestId,
    int documentCount
) implements DomainEvent {
}
