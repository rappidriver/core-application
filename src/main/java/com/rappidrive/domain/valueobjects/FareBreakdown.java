package com.rappidrive.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a detailed breakdown of fare calculation.
 * Immutable - provides transparency on how the final fare was calculated.
 */
public final class FareBreakdown {
    private final Money baseFare;
    private final Money distanceFare;
    private final Money timeFare;
    private final Money subtotal;
    private final double vehicleMultiplier;
    private final double timeMultiplier;
    private final Money totalMultiplier;
    private final Money minimumFare;
    private final Money finalAmount;
    
    public FareBreakdown(Money baseFare, Money distanceFare, Money timeFare,
                        double vehicleMultiplier, double timeMultiplier,
                        Money minimumFare, Money finalAmount) {
        this.baseFare = Objects.requireNonNull(baseFare, "Base fare cannot be null");
        this.distanceFare = Objects.requireNonNull(distanceFare, "Distance fare cannot be null");
        this.timeFare = Objects.requireNonNull(timeFare, "Time fare cannot be null");
        this.vehicleMultiplier = vehicleMultiplier;
        this.timeMultiplier = timeMultiplier;
        this.minimumFare = Objects.requireNonNull(minimumFare, "Minimum fare cannot be null");
        this.finalAmount = Objects.requireNonNull(finalAmount, "Final amount cannot be null");
        
        // Calculate derived values
        this.subtotal = baseFare.add(distanceFare).add(timeFare);
        
        // Calculate total multiplier amount
        BigDecimal combinedMultiplier = BigDecimal.valueOf(vehicleMultiplier)
                .multiply(BigDecimal.valueOf(timeMultiplier));
        this.totalMultiplier = new Money(
                subtotal.getAmount().multiply(combinedMultiplier)
                        .subtract(subtotal.getAmount())
                        .setScale(2, RoundingMode.HALF_UP)
        );
        
        validate();
    }
    
    private void validate() {
        if (vehicleMultiplier <= 0) {
            throw new IllegalArgumentException("Vehicle multiplier must be positive");
        }
        if (timeMultiplier <= 0) {
            throw new IllegalArgumentException("Time multiplier must be positive");
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
        if (minimumFare.isNegative()) {
            throw new IllegalArgumentException("Minimum fare cannot be negative");
        }
        if (finalAmount.isNegative()) {
            throw new IllegalArgumentException("Final amount cannot be negative");
        }
    }
    
    /**
     * Gets a human-readable explanation of the fare calculation.
     *
     * @return formatted string explaining the breakdown
     */
    public String getExplanation() {
        StringBuilder sb = new StringBuilder();
        sb.append("Base Fare: ").append(baseFare).append("\n");
        sb.append("Distance Fare: ").append(distanceFare).append("\n");
        sb.append("Time Fare: ").append(timeFare).append("\n");
        sb.append("Subtotal: ").append(subtotal).append("\n");
        sb.append("Vehicle Multiplier: ").append(vehicleMultiplier).append("x\n");
        sb.append("Time Multiplier: ").append(timeMultiplier).append("x\n");
        sb.append("Total Multiplier Amount: ").append(totalMultiplier).append("\n");
        sb.append("Before Minimum Check: ").append(subtotal.add(totalMultiplier)).append("\n");
        sb.append("Minimum Fare: ").append(minimumFare).append("\n");
        sb.append("Final Amount: ").append(finalAmount);
        return sb.toString();
    }
    
    public Money getBaseFare() {
        return baseFare;
    }
    
    public Money getDistanceFare() {
        return distanceFare;
    }
    
    public Money getTimeFare() {
        return timeFare;
    }
    
    public Money getSubtotal() {
        return subtotal;
    }
    
    public double getVehicleMultiplier() {
        return vehicleMultiplier;
    }
    
    public double getTimeMultiplier() {
        return timeMultiplier;
    }
    
    public Money getTotalMultiplier() {
        return totalMultiplier;
    }
    
    public Money getMinimumFare() {
        return minimumFare;
    }
    
    public Money getFinalAmount() {
        return finalAmount;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FareBreakdown that = (FareBreakdown) o;
        return Double.compare(that.vehicleMultiplier, vehicleMultiplier) == 0 &&
               Double.compare(that.timeMultiplier, timeMultiplier) == 0 &&
               Objects.equals(baseFare, that.baseFare) &&
               Objects.equals(distanceFare, that.distanceFare) &&
               Objects.equals(timeFare, that.timeFare) &&
               Objects.equals(subtotal, that.subtotal) &&
               Objects.equals(totalMultiplier, that.totalMultiplier) &&
               Objects.equals(minimumFare, that.minimumFare) &&
               Objects.equals(finalAmount, that.finalAmount);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(baseFare, distanceFare, timeFare, subtotal,
                vehicleMultiplier, timeMultiplier, totalMultiplier, minimumFare, finalAmount);
    }
    
    @Override
    public String toString() {
        return "FareBreakdown{" +
               "baseFare=" + baseFare +
               ", distanceFare=" + distanceFare +
               ", timeFare=" + timeFare +
               ", subtotal=" + subtotal +
               ", vehicleMultiplier=" + vehicleMultiplier +
               ", timeMultiplier=" + timeMultiplier +
               ", totalMultiplier=" + totalMultiplier +
               ", minimumFare=" + minimumFare +
               ", finalAmount=" + finalAmount +
               '}';
    }
}
