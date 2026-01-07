package com.rappidrive.domain.services;

import com.rappidrive.domain.valueobjects.Currency;
import com.rappidrive.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Standard fare calculation implementation.
 */
public class StandardFareCalculator implements FareCalculator {

    private static final BigDecimal BASE_FARE = new BigDecimal("5.00");
    private static final BigDecimal PRICE_PER_KM = new BigDecimal("2.50");
    private static final BigDecimal PEAK_HOUR_MULTIPLIER = new BigDecimal("1.25");

    @Override
    public Money calculateFare(FareCalculationRequest request) {
        BigDecimal distanceFare = PRICE_PER_KM.multiply(BigDecimal.valueOf(request.distanceKm()));
        BigDecimal subtotal = BASE_FARE.add(distanceFare);

        if (request.isPeakHour()) {
            subtotal = subtotal.multiply(PEAK_HOUR_MULTIPLIER);
        }

        if (request.demandMultiplier() > 1.0) {
            subtotal = subtotal.multiply(BigDecimal.valueOf(request.demandMultiplier()));
        }

        return new Money(subtotal.setScale(2, RoundingMode.HALF_UP), Currency.BRL);
    }
}