package com.rappidrive.domain.events;

import com.rappidrive.domain.valueobjects.DriverId;
import com.rappidrive.domain.valueobjects.PassengerId;
import com.rappidrive.domain.valueobjects.TripId;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a new trip is created.
 */
public record TripCreatedEvent(
    String eventId,
    LocalDateTime occurredOn,
    TripId tripId,
    PassengerId passengerId,
    String origin,
    String destination,
    double estimatedDistanceKm,
    String estimatedFare
) implements DomainEvent {
    
    public TripCreatedEvent(TripId tripId, PassengerId passengerId, String origin, 
                           String destination, double estimatedDistanceKm, String estimatedFare) {
        this(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            tripId,
            passengerId,
            origin,
            destination,
            estimatedDistanceKm,
            estimatedFare
        );
    }
}