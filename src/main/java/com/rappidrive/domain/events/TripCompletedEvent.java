package com.rappidrive.domain.events;

import com.rappidrive.domain.valueobjects.DriverId;
import com.rappidrive.domain.valueobjects.PassengerId;
import com.rappidrive.domain.valueobjects.TripId;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a trip is completed.
 */
public record TripCompletedEvent(
    String eventId,
    LocalDateTime occurredOn,
    TripId tripId,
    PassengerId passengerId,
    DriverId driverId,
    String actualFare,
    long durationMinutes
) implements DomainEvent {
    
    public TripCompletedEvent(TripId tripId, PassengerId passengerId, DriverId driverId,
                             String actualFare, long durationMinutes) {
        this(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            tripId,
            passengerId,
            driverId,
            actualFare,
            durationMinutes
        );
    }
}