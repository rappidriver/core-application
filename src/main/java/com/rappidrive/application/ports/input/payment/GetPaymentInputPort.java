package com.rappidrive.application.ports.input.payment;

import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.valueobjects.TenantId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Input port for retrieving payments.
 */
public interface GetPaymentInputPort {
    
    /**
     * Gets a payment by its ID.
     *
     * @param paymentId the payment ID
     * @return the payment
     */
    Payment execute(UUID paymentId);
    
    /**
     * Finds payments by trip ID.
     *
     * @param tripId the trip ID
     * @return list of payments (should be only one per trip)
     */
    List<Payment> findByTrip(UUID tripId);
    
    /**
     * Finds payments by tenant within a date range.
     *
     * @param tenantId the tenant ID
     * @param from start date
     * @param to end date
     * @return list of payments
     */
    List<Payment> findByTenant(TenantId tenantId, LocalDateTime from, LocalDateTime to);
}
