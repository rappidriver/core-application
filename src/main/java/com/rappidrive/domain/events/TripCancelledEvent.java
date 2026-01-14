package com.rappidrive.domain.events;

import com.rappidrive.domain.valueobjects.ActorType;
import com.rappidrive.domain.valueobjects.CancellationReason;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.TripId;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripCancelledEvent(
    String eventId,
    LocalDateTime occurredOn,
    TripId tripId,
    ActorType cancelledBy,
    CancellationReason reason,
    Money fee,
    LocalDateTime cancelledAt
) implements DomainEvent {
    
    public TripCancelledEvent(TripId tripId, ActorType cancelledBy, CancellationReason reason, Money fee) {
        this(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            tripId,
            cancelledBy,
            reason,
            fee,
            LocalDateTime.now()
        );
    }
}
