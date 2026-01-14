package com.rappidrive.application.ports.input.trip;

import com.rappidrive.domain.valueobjects.ActorType;
import com.rappidrive.domain.valueobjects.CancellationReason;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.TripId;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CancelTripInputPort {

    CancellationResult execute(CancelCommand command);

    record CancelCommand(
        UUID tripId,
        UUID userId,
        ActorType actorType,
        CancellationReason reason,
        String additionalNotes
    ) {}

    record CancellationResult(
        TripId tripId,
        boolean cancelled,
        ActorType cancelledBy,
        CancellationReason reason,
        Money feeCharged,
        boolean refundIssued,
        LocalDateTime cancelledAt,
        String message
    ) {}
}
