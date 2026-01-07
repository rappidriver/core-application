package com.rappidrive.application.ports.input.payment;

import com.rappidrive.domain.entities.Payment;

import java.util.Objects;
import java.util.UUID;

/**
 * Input port for refunding payments.
 */
public interface RefundPaymentInputPort {
    
    /**
     * Refunds a payment.
     *
     * @param paymentId the payment ID
     * @param reason the refund reason
     * @return the refunded payment
     */
    Payment execute(UUID paymentId, String reason);
    
    /**
     * Command for refunding a payment.
     */
    record RefundPaymentCommand(
            UUID paymentId,
            String reason
    ) {
        public RefundPaymentCommand {
            Objects.requireNonNull(paymentId, "Payment ID cannot be null");
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Refund reason cannot be null or empty");
            }
        }
    }
}
