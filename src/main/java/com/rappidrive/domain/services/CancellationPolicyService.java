package com.rappidrive.domain.services;

import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.enums.TripStatus;
import com.rappidrive.domain.valueobjects.ActorType;
import com.rappidrive.domain.valueobjects.CancellationFee;
import com.rappidrive.domain.valueobjects.Currency;
import com.rappidrive.domain.valueobjects.Money;

import java.time.Duration;
import java.time.LocalDateTime;

public class CancellationPolicyService {

    private static final long PASSENGER_FREE_WINDOW_REQUESTED_MINUTES = 5;
    private static final long PASSENGER_FREE_WINDOW_ASSIGNED_MINUTES = 2;
    private static final double PASSENGER_FEE_REQUESTED = 5.0;
    private static final double PASSENGER_FEE_ASSIGNED = 8.0;

    public CancellationFee calculateFee(Trip trip, ActorType actor, LocalDateTime cancelledAt) {
        if (actor == ActorType.DRIVER) {
            return CancellationFee.free("Motorista não paga taxa de cancelamento");
        }

        return calculatePassengerFee(trip, cancelledAt);
    }

    private CancellationFee calculatePassengerFee(Trip trip, LocalDateTime cancelledAt) {
        TripStatus status = trip.getStatus();
        LocalDateTime createdAt = trip.getCreatedAt();
        Duration elapsed = Duration.between(createdAt, cancelledAt);

        if (status == TripStatus.REQUESTED) {
            if (elapsed.toMinutes() < PASSENGER_FREE_WINDOW_REQUESTED_MINUTES) {
                return CancellationFee.free("Cancelamento gratuito (dentro de " + PASSENGER_FREE_WINDOW_REQUESTED_MINUTES + " minutos)");
            }
            return CancellationFee.of(Money.of(PASSENGER_FEE_REQUESTED, Currency.BRL), 
                "Cancelamento após " + PASSENGER_FREE_WINDOW_REQUESTED_MINUTES + " minutos da solicitação");
        }

        if (status == TripStatus.DRIVER_ASSIGNED) {
            LocalDateTime assignedAt = trip.getAssignedAt();
            if (assignedAt == null) {
                assignedAt = createdAt;
            }
            Duration elapsedSinceAssignment = Duration.between(assignedAt, cancelledAt);
            
            if (elapsedSinceAssignment.toMinutes() < PASSENGER_FREE_WINDOW_ASSIGNED_MINUTES) {
                return CancellationFee.free("Cancelamento gratuito (dentro de " + PASSENGER_FREE_WINDOW_ASSIGNED_MINUTES + " minutos da atribuição)");
            }
            return CancellationFee.of(Money.of(PASSENGER_FEE_ASSIGNED, Currency.BRL), 
                "Cancelamento após " + PASSENGER_FREE_WINDOW_ASSIGNED_MINUTES + " minutos da atribuição");
        }

        return CancellationFee.free("Status não sujeito a taxa");
    }
}
