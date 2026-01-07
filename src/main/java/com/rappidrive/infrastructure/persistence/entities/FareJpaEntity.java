package com.rappidrive.infrastructure.persistence.entities;

import com.rappidrive.domain.enums.FareMultiplierType;
import com.rappidrive.domain.enums.VehicleType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for Fare persistence.
 * Maps to the 'fares' table.
 */
@Entity
@Table(name = "fares")
public class FareJpaEntity {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "trip_id", nullable = false, unique = true)
    private UUID tripId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "base_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;
    
    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;
    
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    @Column(name = "distance_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceFare;
    
    @Column(name = "time_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal timeFare;
    
    @Column(name = "multiplier_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FareMultiplierType multiplierType;
    
    @Column(name = "time_multiplier", nullable = false, precision = 3, scale = 2)
    private BigDecimal timeMultiplier;
    
    @Column(name = "vehicle_category", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleCategory;
    
    @Column(name = "vehicle_multiplier", nullable = false, precision = 3, scale = 2)
    private BigDecimal vehicleMultiplier;
    
    @Column(name = "total_before_multiplier", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalBeforeMultiplier;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "minimum_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumFare;
    
    @Column(name = "explanation", nullable = false, length = 500)
    private String explanation;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    
    public FareJpaEntity() {
    }
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getTripId() {
        return tripId;
    }
    
    public void setTripId(UUID tripId) {
        this.tripId = tripId;
    }
    
    public UUID getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
    
    public BigDecimal getBaseFare() {
        return baseFare;
    }
    
    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }
    
    public Double getDistanceKm() {
        return distanceKm;
    }
    
    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public BigDecimal getDistanceFare() {
        return distanceFare;
    }
    
    public void setDistanceFare(BigDecimal distanceFare) {
        this.distanceFare = distanceFare;
    }
    
    public BigDecimal getTimeFare() {
        return timeFare;
    }
    
    public void setTimeFare(BigDecimal timeFare) {
        this.timeFare = timeFare;
    }
    
    public FareMultiplierType getMultiplierType() {
        return multiplierType;
    }
    
    public void setMultiplierType(FareMultiplierType multiplierType) {
        this.multiplierType = multiplierType;
    }
    
    public BigDecimal getTimeMultiplier() {
        return timeMultiplier;
    }
    
    public void setTimeMultiplier(BigDecimal timeMultiplier) {
        this.timeMultiplier = timeMultiplier;
    }
    
    public VehicleType getVehicleCategory() {
        return vehicleCategory;
    }
    
    public void setVehicleCategory(VehicleType vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
    }
    
    public BigDecimal getVehicleMultiplier() {
        return vehicleMultiplier;
    }
    
    public void setVehicleMultiplier(BigDecimal vehicleMultiplier) {
        this.vehicleMultiplier = vehicleMultiplier;
    }
    
    public BigDecimal getTotalBeforeMultiplier() {
        return totalBeforeMultiplier;
    }
    
    public void setTotalBeforeMultiplier(BigDecimal totalBeforeMultiplier) {
        this.totalBeforeMultiplier = totalBeforeMultiplier;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getMinimumFare() {
        return minimumFare;
    }
    
    public void setMinimumFare(BigDecimal minimumFare) {
        this.minimumFare = minimumFare;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }
    
    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
