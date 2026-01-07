package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.valueobjects.TenantId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for payment persistence operations.
 */
public interface PaymentRepositoryPort {
    
    /**
     * Saves a payment.
     *
     * @param payment the payment to save
     * @return the saved payment
     */
    Payment save(Payment payment);
    
    /**
     * Finds a payment by its ID.
     *
     * @param id the payment ID
     * @return an Optional containing the payment if found
     */
    Optional<Payment> findById(UUID id);
    
    /**
     * Finds a payment by trip ID.
     *
     * @param tripId the trip ID
     * @return an Optional containing the payment if found
     */
    Optional<Payment> findByTripId(UUID tripId);
    
    /**
     * Finds all payments for a tenant within a date range.
     *
     * @param tenantId the tenant ID
     * @param from start date
     * @param to end date
     * @return list of payments
     */
    List<Payment> findByTenantIdAndCreatedAtBetween(TenantId tenantId, LocalDateTime from, LocalDateTime to);
    
    /**
     * Finds payments by tenant ID.
     *
     * @param tenantId the tenant ID
     * @return list of payments
     */
    List<Payment> findByTenantId(TenantId tenantId);
    
    /**
     * Checks if a payment exists for a trip.
     *
     * @param tripId the trip ID
     * @return true if a payment exists for the trip
     */
    boolean existsByTripId(UUID tripId);
    
    /**
     * Deletes a payment.
     *
     * @param payment the payment to delete
     */
    void delete(Payment payment);
}
