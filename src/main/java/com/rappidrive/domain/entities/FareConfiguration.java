package com.rappidrive.domain.entities;

import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.TenantId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Fare configuration entity - defines pricing rules per tenant.
 * Aggregate root for fare pricing configuration.
 */
public class FareConfiguration {
    private final UUID id;
    private final TenantId tenantId;
    private Money baseFare;
    private Money pricePerKm;
    private Money pricePerMinute;
    private Money minimumFare;
    private double platformCommissionRate;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public FareConfiguration(UUID id, TenantId tenantId, Money baseFare, Money pricePerKm,
                            Money pricePerMinute, Money minimumFare, double platformCommissionRate,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.baseFare = Objects.requireNonNull(baseFare, "Base fare cannot be null");
        this.pricePerKm = Objects.requireNonNull(pricePerKm, "Price per km cannot be null");
        this.pricePerMinute = Objects.requireNonNull(pricePerMinute, "Price per minute cannot be null");
        this.minimumFare = Objects.requireNonNull(minimumFare, "Minimum fare cannot be null");
        this.platformCommissionRate = platformCommissionRate;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
        
        validate();
    }
    
    /**
     * Creates a new fare configuration with default values.
     */
    public static FareConfiguration create(TenantId tenantId, Money baseFare, Money pricePerKm,
                                          Money pricePerMinute, Money minimumFare,
                                          double platformCommissionRate) {
        LocalDateTime now = LocalDateTime.now();
        return new FareConfiguration(
                UUID.randomUUID(),
                tenantId,
                baseFare,
                pricePerKm,
                pricePerMinute,
                minimumFare,
                platformCommissionRate,
                now,
                now
        );
    }
    
    private void validate() {
        if (baseFare.isNegativeOrZero()) {
            throw new IllegalArgumentException("Base fare must be positive");
        }
        if (pricePerKm.isNegativeOrZero()) {
            throw new IllegalArgumentException("Price per km must be positive");
        }
        if (pricePerMinute.isNegativeOrZero()) {
            throw new IllegalArgumentException("Price per minute must be positive");
        }
        if (minimumFare.isNegativeOrZero()) {
            throw new IllegalArgumentException("Minimum fare must be positive");
        }
        if (minimumFare.isLessThan(baseFare)) {
            throw new IllegalArgumentException("Minimum fare must be greater than or equal to base fare");
        }
        if (platformCommissionRate < 0.0 || platformCommissionRate > 1.0) {
            throw new IllegalArgumentException("Platform commission rate must be between 0.0 and 1.0");
        }
    }
    
    /**
     * Updates the base fare.
     */
    public void updateBaseFare(Money baseFare) {
        this.baseFare = Objects.requireNonNull(baseFare, "Base fare cannot be null");
        if (baseFare.isNegativeOrZero()) {
            throw new IllegalArgumentException("Base fare must be positive");
        }
        if (minimumFare.isLessThan(baseFare)) {
            throw new IllegalArgumentException("Base fare cannot be greater than minimum fare");
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Updates the price per kilometer.
     */
    public void updatePricePerKm(Money pricePerKm) {
        this.pricePerKm = Objects.requireNonNull(pricePerKm, "Price per km cannot be null");
        if (pricePerKm.isNegativeOrZero()) {
            throw new IllegalArgumentException("Price per km must be positive");
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Updates the price per minute.
     */
    public void updatePricePerMinute(Money pricePerMinute) {
        this.pricePerMinute = Objects.requireNonNull(pricePerMinute, "Price per minute cannot be null");
        if (pricePerMinute.isNegativeOrZero()) {
            throw new IllegalArgumentException("Price per minute must be positive");
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Updates the minimum fare.
     */
    public void updateMinimumFare(Money minimumFare) {
        this.minimumFare = Objects.requireNonNull(minimumFare, "Minimum fare cannot be null");
        if (minimumFare.isNegativeOrZero()) {
            throw new IllegalArgumentException("Minimum fare must be positive");
        }
        if (minimumFare.isLessThan(baseFare)) {
            throw new IllegalArgumentException("Minimum fare must be greater than or equal to base fare");
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Updates the platform commission rate.
     */
    public void updateCommissionRate(double rate) {
        if (rate < 0.0 || rate > 1.0) {
            throw new IllegalArgumentException("Platform commission rate must be between 0.0 and 1.0");
        }
        this.platformCommissionRate = rate;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculates the platform fee based on the total amount.
     */
    public Money calculatePlatformFee(Money totalAmount) {
        Objects.requireNonNull(totalAmount, "Total amount cannot be null");
        BigDecimal feeAmount = totalAmount.getAmount()
                .multiply(BigDecimal.valueOf(platformCommissionRate))
                .setScale(2, RoundingMode.HALF_UP);
        return new Money(feeAmount);
    }
    
    /**
     * Calculates the driver amount based on the total amount.
     */
    public Money calculateDriverAmount(Money totalAmount) {
        Objects.requireNonNull(totalAmount, "Total amount cannot be null");
        Money platformFee = calculatePlatformFee(totalAmount);
        return totalAmount.subtract(platformFee);
    }
    
    /**
     * Checks if this configuration belongs to the given tenant.
     */
    public boolean belongsToTenant(TenantId tenantId) {
        return this.tenantId.equals(tenantId);
    }
    
    public UUID getId() {
        return id;
    }
    
    public TenantId getTenantId() {
        return tenantId;
    }
    
    public Money getBaseFare() {
        return baseFare;
    }
    
    public Money getPricePerKm() {
        return pricePerKm;
    }
    
    public Money getPricePerMinute() {
        return pricePerMinute;
    }
    
    public Money getMinimumFare() {
        return minimumFare;
    }
    
    public double getPlatformCommissionRate() {
        return platformCommissionRate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FareConfiguration that = (FareConfiguration) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "FareConfiguration{" +
               "id=" + id +
               ", tenantId=" + tenantId +
               ", baseFare=" + baseFare +
               ", pricePerKm=" + pricePerKm +
               ", pricePerMinute=" + pricePerMinute +
               ", minimumFare=" + minimumFare +
               ", platformCommissionRate=" + platformCommissionRate +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
