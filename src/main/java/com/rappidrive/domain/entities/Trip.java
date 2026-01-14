package com.rappidrive.domain.entities;

import com.rappidrive.domain.enums.TripStatus;
import com.rappidrive.domain.exceptions.InvalidTripStateException;
import com.rappidrive.domain.trip.TripPaymentStatus;
import com.rappidrive.domain.valueobjects.*;
import com.rappidrive.domain.events.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Trip entity representing a ride request in the RappiDrive platform.
 * Aggregate root for trip lifecycle management.
 */
public class Trip {

    private final TripId id;
    private final TenantId tenantId;
    private final PassengerId passengerId;
    private DriverId driverId;
    private final Location origin;
    private final Location destination;
    private TripStatus status;
    private final double estimatedDistanceKm;
    private final Money estimatedFare;
    private Money actualFare;

    private static final double BASE_FARE = 5.0;
    private static final double PRICE_PER_KM = 2.5;
    private final LocalDateTime requestedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String cancellationReason;
    private ActorType cancelledBy;
    private CancellationReason cancellationReasonEnum;
    private LocalDateTime cancelledAt;
    private LocalDateTime updatedAt;

    private int version;
    private UUID fareId;
    private UUID paymentId;
    private TripPaymentStatus paymentStatus;

    public Trip(TripId id, TenantId tenantId, PassengerId passengerId, Location origin, Location destination) {
        if (id == null || tenantId == null || passengerId == null || origin == null || destination == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
        this.id = id;
        this.tenantId = tenantId;
        this.passengerId = passengerId;
        this.origin = origin;
        this.destination = destination;
        this.status = TripStatus.REQUESTED;
        this.estimatedDistanceKm = origin.distanceTo(destination);
        this.estimatedFare = computeEstimatedFare(this.estimatedDistanceKm);
        this.requestedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor that accepts an estimated fare (used when creating a new Trip).
     */
    public Trip(TripId id, TenantId tenantId, PassengerId passengerId, Location origin, Location destination, Money estimatedFare) {
        if (id == null || tenantId == null || passengerId == null || origin == null || destination == null || estimatedFare == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
        this.id = id;
        this.tenantId = tenantId;
        this.passengerId = passengerId;
        this.origin = origin;
        this.destination = destination;
        this.status = TripStatus.REQUESTED;
        this.estimatedDistanceKm = origin.distanceTo(destination);
        this.estimatedFare = estimatedFare;
        this.requestedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Reconstruction constructor used by persistence mappers.
     */
    public Trip(TripId id,
                TenantId tenantId,
                PassengerId passengerId,
                DriverId driverId,
                Location origin,
                Location destination,
                TripStatus status,
                LocalDateTime requestedAt,
                LocalDateTime startedAt,
                LocalDateTime completedAt,
                UUID fareId,
                UUID paymentId,
                TripPaymentStatus paymentStatus) {
        this.id = id;
        this.tenantId = tenantId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.origin = origin;
        this.destination = destination;
        this.status = status != null ? status : TripStatus.REQUESTED;
        this.estimatedDistanceKm = origin != null ? origin.distanceTo(destination) : 0.0;
        this.estimatedFare = computeEstimatedFare(this.estimatedDistanceKm);
        this.requestedAt = requestedAt != null ? requestedAt : LocalDateTime.now();
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.fareId = fareId;
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignDriver(DriverId driverId) {
        if (driverId == null) {
            throw new IllegalArgumentException("Driver ID cannot be null");
        }
        if (status != TripStatus.REQUESTED) {
            throw new InvalidTripStateException("Can only assign driver to requested trips");
        }
        this.driverId = driverId;
        this.status = TripStatus.DRIVER_ASSIGNED;
        this.assignedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == TripStatus.REQUESTED;
    }

    public void complete(Money actualFare) {
        if (actualFare == null) {
            throw new IllegalArgumentException("Actual fare cannot be null");
        }
        if (status != TripStatus.IN_PROGRESS) {
            throw new InvalidTripStateException("Can only complete trips that are in progress");
        }
        this.status = TripStatus.COMPLETED;
        this.actualFare = actualFare;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void start() {
        if (status != TripStatus.DRIVER_ASSIGNED) {
            throw new InvalidTripStateException("Can only start trips with assigned driver");
        }
        this.status = TripStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void completeWithPayment(com.rappidrive.domain.entities.Fare fare, com.rappidrive.domain.entities.Payment payment) {
        if (fare == null || payment == null) {
            throw new IllegalArgumentException("Fare and payment cannot be null");
        }
        if (status != TripStatus.IN_PROGRESS) {
            throw new InvalidTripStateException("Can only complete trips that are in progress");
        }

        this.actualFare = fare.getTotalAmount();
        this.fareId = fare.getId();
        this.paymentId = payment.getId();
        this.paymentStatus = payment.getStatus() != null ? com.rappidrive.domain.trip.TripPaymentStatus.PAID : com.rappidrive.domain.trip.TripPaymentStatus.PAYMENT_FAILED;
        this.status = TripStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void registerEvent(DomainEvent event) {
        DomainEventPublisher.instance().publish(event);
    }

    public TripId getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Optional<Money> getActualFare() {
        return Optional.ofNullable(actualFare);
    }

    public Optional<UUID> getFareId() {
        return Optional.ofNullable(fareId);
    }

    public Optional<UUID> getPaymentId() {
        return Optional.ofNullable(paymentId);
    }

    public TripPaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public Optional<LocalDateTime> getStartedAt() {
        return Optional.ofNullable(startedAt);
    }

    public Optional<LocalDateTime> getCompletedAt() {
        return Optional.ofNullable(completedAt);
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public PassengerId getPassengerId() {
        return passengerId;
    }

    public Optional<UUID> getDriverId() {
        return Optional.ofNullable(driverId != null ? driverId.getValue() : null);
    }

    public DriverId getDriverIdValueObject() {
        return driverId;
    }

    public Location getOrigin() {
        return origin;
    }

    public Location getDestination() {
        return destination;
    }

    public TripStatus getStatus() {
        return status;
    }

    public double getEstimatedDistanceKm() {
        return estimatedDistanceKm;
    }

    public Money getEstimatedFare() {
        return estimatedFare;
    }

    private Money computeEstimatedFare(double distanceKm) {
        if (distanceKm <= 0) {
            return new Money(0.0);
        }
        double amount = BASE_FARE + (distanceKm * PRICE_PER_KM);
        return new Money(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(id, trip.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Trip(id=%s, passenger=%s, status=%s)", id, passengerId, status);
    }

    public Optional<String> getCancellationReason() {
        return Optional.ofNullable(cancellationReason);
    }

    public Optional<ActorType> getCancelledBy() {
        return Optional.ofNullable(cancelledBy);
    }

    public Optional<CancellationReason> getCancellationReasonEnum() {
        return Optional.ofNullable(cancellationReasonEnum);
    }

    public Optional<LocalDateTime> getCancelledAt() {
        return Optional.ofNullable(cancelledAt);
    }

    public LocalDateTime getCreatedAt() {
        return requestedAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    /**
     * Cancels the trip with a provided reason.
     * Validates reason and current trip state.
     */
    public void cancel(String reason) {
        if (reason == null) {
            throw new IllegalArgumentException("Cancellation reason cannot be null");
        }
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason cannot be blank");
        }
        if (this.status == TripStatus.COMPLETED) {
            throw new InvalidTripStateException("Cannot cancel a completed trip");
        }
        if (this.status == TripStatus.CANCELLED) {
            throw new InvalidTripStateException("Trip is already cancelled");
        }

        this.cancellationReason = reason.trim();
        this.status = TripStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(ActorType actor, CancellationReason reason, LocalDateTime cancelledAt) {
        if (actor == null) {
            throw new IllegalArgumentException("Actor cannot be null");
        }
        if (reason == null) {
            throw new IllegalArgumentException("Cancellation reason cannot be null");
        }
        if (cancelledAt == null) {
            throw new IllegalArgumentException("Cancelled at cannot be null");
        }
        if (this.status == TripStatus.COMPLETED) {
            throw new InvalidTripStateException("Cannot cancel a completed trip");
        }
        if (this.status == TripStatus.CANCELLED) {
            throw new InvalidTripStateException("Trip is already cancelled");
        }
        if (this.status == TripStatus.IN_PROGRESS) {
            throw new InvalidTripStateException("Cannot cancel a trip in progress");
        }

        this.cancelledBy = actor;
        this.cancellationReasonEnum = reason;
        this.cancellationReason = reason.getDescription();
        this.cancelledAt = cancelledAt;
        this.status = TripStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();

        DomainEventsCollector.instance().handle(
            new TripCancelledEvent(this.id, actor, reason, Money.zero(Currency.BRL))
        );
    }

    /**
     * Returns the duration between start and completion if available.
     */
    public Optional<Duration> getDuration() {
        if (this.startedAt == null || this.completedAt == null) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(this.startedAt, this.completedAt));
    }
}
