package com.rappidrive.infrastructure.persistence.entities;

import com.rappidrive.domain.enums.TripStatus;
import com.rappidrive.domain.valueobjects.TenantId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for Trip.
 * This is part of the infrastructure layer and should NOT be exposed to domain or application layers.
 */
@Entity
@Table(name = "trips")
@Builder
public class TripJpaEntity {

    public TripJpaEntity() {}

    public TripJpaEntity(UUID id,
                         TenantId tenantId,
                         UUID passengerId,
                         UUID driverId,
                         Integer version,
                         Double pickupLatitude,
                         Double pickupLongitude,
                         Double dropoffLatitude,
                         Double dropoffLongitude,
                         TripStatus status,
                         Double distanceKm,
                         BigDecimal fareAmount,
                         UUID fareId,
                         UUID paymentId,
                         String paymentStatus,
                         LocalDateTime requestedAt,
                         LocalDateTime acceptedAt,
                         LocalDateTime startedAt,
                         LocalDateTime completedAt,
                         LocalDateTime cancelledAt,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.version = version;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.dropoffLatitude = dropoffLatitude;
        this.dropoffLongitude = dropoffLongitude;
        this.status = status;
        this.distanceKm = distanceKm;
        this.fareAmount = fareAmount;
        this.fareId = fareId;
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
        this.requestedAt = requestedAt;
        this.acceptedAt = acceptedAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.cancelledAt = cancelledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    @Id
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private TenantId tenantId;
    
    @Column(name = "passenger_id", nullable = false)
    private UUID passengerId;
    
    @Column(name = "driver_id")
    private UUID driverId;
    
    @Version
    private Integer version = 0;
    
    // Locations
    @Column(name = "pickup_latitude", nullable = false)
    private Double pickupLatitude;
    
    @Column(name = "pickup_longitude", nullable = false)
    private Double pickupLongitude;
    
    @Column(name = "dropoff_latitude", nullable = false)
    private Double dropoffLatitude;
    
    @Column(name = "dropoff_longitude", nullable = false)
    private Double dropoffLongitude;
    
    // Trip details
    @Column(nullable = false, length = 20)
    private TripStatus status;
    
    @Column(name = "distance_km")
    private Double distanceKm;
    
    @Column(name = "fare_amount", precision = 10, scale = 2)
    private BigDecimal fareAmount;
    
    // Payment integration fields
    @Column(name = "fare_id")
    private UUID fareId;
    
    @Column(name = "payment_id")
    private UUID paymentId;
    
    @Column(name = "payment_status", length = 20)
    private String paymentStatus; // PENDING, PROCESSING, PAID, PAYMENT_FAILED, REFUNDED
    
    // Timestamps
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Explicit setters/getters required by mappers (avoid relying on Lombok generation at compile-time)
    public UUID getId() { return this.id; }
    public void setId(UUID id) { this.id = id; }

    public TenantId getTenantId() { return this.tenantId; }
    public void setTenantId(TenantId tenantId) { this.tenantId = tenantId; }

    public UUID getPassengerId() { return this.passengerId; }
    public void setPassengerId(UUID passengerId) { this.passengerId = passengerId; }

    public UUID getDriverId() { return this.driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public Double getPickupLatitude() { return this.pickupLatitude; }
    public void setPickupLatitude(Double val) { this.pickupLatitude = val; }
    public Double getPickupLongitude() { return this.pickupLongitude; }
    public void setPickupLongitude(Double val) { this.pickupLongitude = val; }
    public Double getDropoffLatitude() { return this.dropoffLatitude; }
    public void setDropoffLatitude(Double val) { this.dropoffLatitude = val; }
    public Double getDropoffLongitude() { return this.dropoffLongitude; }
    public void setDropoffLongitude(Double val) { this.dropoffLongitude = val; }

    public TripStatus getStatus() { return this.status; }
    public void setStatus(TripStatus status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return this.requestedAt; }
    public void setRequestedAt(LocalDateTime t) { this.requestedAt = t; }

    public LocalDateTime getStartedAt() { return this.startedAt; }
    public void setStartedAt(LocalDateTime t) { this.startedAt = t; }

    public LocalDateTime getCompletedAt() { return this.completedAt; }
    public void setCompletedAt(LocalDateTime t) { this.completedAt = t; }

    public UUID getFareId() { return this.fareId; }
    public void setFareId(UUID id) { this.fareId = id; }

    public UUID getPaymentId() { return this.paymentId; }
    public void setPaymentId(UUID id) { this.paymentId = id; }

    public String getPaymentStatus() { return this.paymentStatus; }
    public void setPaymentStatus(String s) { this.paymentStatus = s; }

    public Double getDistanceKm() { return this.distanceKm; }
    public void setDistanceKm(Double d) { this.distanceKm = d; }

    public BigDecimal getFareAmount() { return this.fareAmount; }
    public void setFareAmount(BigDecimal amt) { this.fareAmount = amt; }
}
