package com.rappidrive.application.ports.input.payment;

import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.valueobjects.PaymentMethod;

import java.util.Objects;
import java.util.UUID;

/**
 * Input port for processing payments.
 */
public interface ProcessPaymentInputPort {
    
    /**
     * Processes a payment for a trip.
     *
     * @param command the payment command
     * @return the processed payment
     */
    Payment execute(ProcessPaymentCommand command);
    
    /**
     * Command for processing a payment.
     */
    record ProcessPaymentCommand(
            UUID tripId,
            PaymentMethod paymentMethod
    ) {
        public ProcessPaymentCommand {
            Objects.requireNonNull(tripId, "Trip ID cannot be null");
            Objects.requireNonNull(paymentMethod, "Payment method cannot be null");
        }
    }
}
