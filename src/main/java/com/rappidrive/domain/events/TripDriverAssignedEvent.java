package com.rappidrive.domain.events;

import com.rappidrive.domain.valueobjects.DriverId;
import com.rappidrive.domain.valueobjects.TripId;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a driver is assigned to a trip.
 */
public record TripDriverAssignedEvent(
    String eventId,
    LocalDateTime occurredOn,
    TripId tripId,
    DriverId driverId
) implements DomainEvent {

    public TripDriverAssignedEvent(TripId tripId, DriverId driverId) {
        this(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            tripId,
            driverId
        );
    }
}
