package com.rappidrive.domain.services;

import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.Money;

/**
 * Domain Service for calculating trip fares.
 * Strategy pattern allowing different pricing models.
 */
public interface FareCalculator {

    /**
     * Calculates fare for a trip based on various factors.
     *
     * @param request fare calculation parameters
     * @return calculated fare
     */
    Money calculateFare(FareCalculationRequest request);

    /**
     * Request object for fare calculation containing all necessary parameters.
     */
    record FareCalculationRequest(
        Location origin,
        Location destination,
        double distanceKm,
        boolean isPeakHour,
        double demandMultiplier
    ) {}
}