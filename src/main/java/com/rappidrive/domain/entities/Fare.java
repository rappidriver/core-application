package com.rappidrive.domain.entities;

import com.rappidrive.domain.enums.FareMultiplierType;
import com.rappidrive.domain.enums.VehicleType;
import com.rappidrive.domain.exceptions.InvalidFareException;
import com.rappidrive.domain.valueobjects.FareBreakdown;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.TenantId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Fare entity - represents a calculated fare for a trip.
 * Contains the breakdown of how the fare was calculated.
 */
public class Fare {
    private final UUID id;
    private final UUID tripId;
    private final TenantId tenantId;
    private final Money baseFare;
    private final double distanceKm;
    private final int durationMinutes;
    private final Money distanceFare;
    private final Money timeFare;
    private final FareMultiplierType multiplierType;
    private final VehicleType vehicleCategory;
    private final Money totalBeforeMultiplier;
    private final Money totalAmount;
    private final FareBreakdown breakdown;
    private final LocalDateTime calculatedAt;
    
    public Fare(UUID id, UUID tripId, TenantId tenantId, Money baseFare,
               double distanceKm, int durationMinutes, Money distanceFare, Money timeFare,
               FareMultiplierType multiplierType, VehicleType vehicleCategory,
               Money totalBeforeMultiplier, Money totalAmount, FareBreakdown breakdown,
               LocalDateTime calculatedAt) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.tripId = Objects.requireNonNull(tripId, "Trip ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.baseFare = Objects.requireNonNull(baseFare, "Base fare cannot be null");
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.distanceFare = Objects.requireNonNull(distanceFare, "Distance fare cannot be null");
        this.timeFare = Objects.requireNonNull(timeFare, "Time fare cannot be null");
        this.multiplierType = Objects.requireNonNull(multiplierType, "Multiplier type cannot be null");
        this.vehicleCategory = Objects.requireNonNull(vehicleCategory, "Vehicle category cannot be null");
        this.totalBeforeMultiplier = Objects.requireNonNull(totalBeforeMultiplier, "Total before multiplier cannot be null");
        this.totalAmount = Objects.requireNonNull(totalAmount, "Total amount cannot be null");
        this.breakdown = Objects.requireNonNull(breakdown, "Breakdown cannot be null");
        this.calculatedAt = Objects.requireNonNull(calculatedAt, "Calculated at cannot be null");
        
        validate();
    }
    
    /**
     * Calculates a new fare based on the configuration and trip details.
     *
     * @param config the fare configuration
     * @param tripId the trip ID
     * @param tenantId the tenant ID
     * @param distanceKm the distance in kilometers
     * @param durationMinutes the duration in minutes
     * @param vehicleCategory the vehicle category
     * @param tripTime the time when the trip occurred
     * @return a new Fare instance
     */
    public static Fare calculate(FareConfiguration config, UUID tripId, TenantId tenantId,
                                double distanceKm, int durationMinutes,
                                VehicleType vehicleCategory, LocalDateTime tripTime) {
        Objects.requireNonNull(config, "Fare configuration cannot be null");
        Objects.requireNonNull(tripId, "Trip ID cannot be null");
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(vehicleCategory, "Vehicle category cannot be null");
        Objects.requireNonNull(tripTime, "Trip time cannot be null");
        
        if (distanceKm < 0) {
            throw InvalidFareException.negativeDistance();
        }
        if (durationMinutes < 0) {
            throw InvalidFareException.negativeDuration();
        }
        
        // Base fare
        Money baseFare = config.getBaseFare();
        
        // Distance fare: distance × pricePerKm
        Money distanceFare = config.getPricePerKm().multiply(BigDecimal.valueOf(distanceKm));
        
        // Time fare: duration × pricePerMinute
        Money timeFare = config.getPricePerMinute().multiply(BigDecimal.valueOf(durationMinutes));
        
        // Subtotal before multipliers
        Money subtotal = baseFare.add(distanceFare).add(timeFare);
        
        // Determine time multiplier
        FareMultiplierType multiplierType = FareMultiplierType.fromTripTime(tripTime);
        double timeMultiplier = multiplierType.getMultiplier();
        
        // Determine vehicle multiplier
        double vehicleMultiplier = getVehicleMultiplier(vehicleCategory);
        
        // Apply multipliers
        BigDecimal combinedMultiplier = BigDecimal.valueOf(vehicleMultiplier)
                .multiply(BigDecimal.valueOf(timeMultiplier));
        Money totalAfterMultipliers = subtotal.multiply(combinedMultiplier);
        
        // Ensure minimum fare
        Money minimumFare = config.getMinimumFare();
        Money finalAmount = totalAfterMultipliers.isLessThan(minimumFare)
                ? minimumFare
                : totalAfterMultipliers;
        
        // Round up to nearest 10 cents (R$ 0.10)
        finalAmount = roundUpToNearestTenCents(finalAmount);
        
        // Create breakdown
        FareBreakdown breakdown = new FareBreakdown(
                baseFare,
                distanceFare,
                timeFare,
                vehicleMultiplier,
                timeMultiplier,
                minimumFare,
                finalAmount
        );
        
        return new Fare(
                UUID.randomUUID(),
                tripId,
                tenantId,
                baseFare,
                distanceKm,
                durationMinutes,
                distanceFare,
                timeFare,
                multiplierType,
                vehicleCategory,
                subtotal,
                finalAmount,
                breakdown,
                LocalDateTime.now()
        );
    }
    
    /**
     * Gets the vehicle category multiplier.
     * HATCHBACK: 0.9x, SEDAN: 1.0x, SUV: 1.2x
     */
    private static double getVehicleMultiplier(VehicleType vehicleType) {
        return switch (vehicleType) {
            case HATCHBACK -> 0.9;
            case SEDAN -> 1.0;
            case SUV -> 1.2;
        };
    }
    
    /**
     * Rounds up the amount to the nearest 10 cents (R$ 0.10).
     * Examples: R$ 12.34 → R$ 12.40, R$ 12.30 → R$ 12.30, R$ 12.01 → R$ 12.10
     */
    private static Money roundUpToNearestTenCents(Money amount) {
        BigDecimal value = amount.getAmount();
        BigDecimal cents = value.multiply(BigDecimal.valueOf(100));
        BigDecimal roundedCents = cents.divide(BigDecimal.TEN, 0, RoundingMode.CEILING)
                .multiply(BigDecimal.TEN);
        BigDecimal roundedValue = roundedCents.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new Money(roundedValue);
    }
    
    private void validate() {
        if (distanceKm < 0) {
            throw InvalidFareException.negativeDistance();
        }
        if (durationMinutes < 0) {
            throw InvalidFareException.negativeDuration();
        }
        if (baseFare.isNegative()) {
            throw new IllegalArgumentException("Base fare cannot be negative");
        }
        if (distanceFare.isNegative()) {
            throw new IllegalArgumentException("Distance fare cannot be negative");
        }
        if (timeFare.isNegative()) {
            throw new IllegalArgumentException("Time fare cannot be negative");
        }
        if (totalAmount.isNegativeOrZero()) {
            throw new IllegalArgumentException("Total amount must be positive");
        }
    }
    
    public UUID getId() {
        return id;
    }
    
    public UUID getTripId() {
        return tripId;
    }
    
    public TenantId getTenantId() {
        return tenantId;
    }
    
    public Money getBaseFare() {
        return baseFare;
    }
    
    public double getDistanceKm() {
        return distanceKm;
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    public Money getDistanceFare() {
        return distanceFare;
    }
    
    public Money getTimeFare() {
        return timeFare;
    }
    
    public FareMultiplierType getMultiplierType() {
        return multiplierType;
    }
    
    public VehicleType getVehicleCategory() {
        return vehicleCategory;
    }
    
    public Money getTotalBeforeMultiplier() {
        return totalBeforeMultiplier;
    }
    
    public Money getTotalAmount() {
        return totalAmount;
    }
    
    public FareBreakdown getBreakdown() {
        return breakdown;
    }
    
    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fare fare = (Fare) o;
        return Objects.equals(id, fare.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Fare{" +
               "id=" + id +
               ", tripId=" + tripId +
               ", tenantId=" + tenantId +
               ", baseFare=" + baseFare +
               ", distanceKm=" + distanceKm +
               ", durationMinutes=" + durationMinutes +
               ", distanceFare=" + distanceFare +
               ", timeFare=" + timeFare +
               ", multiplierType=" + multiplierType +
               ", vehicleCategory=" + vehicleCategory +
               ", totalBeforeMultiplier=" + totalBeforeMultiplier +
               ", totalAmount=" + totalAmount +
               ", calculatedAt=" + calculatedAt +
               '}';
    }
}
